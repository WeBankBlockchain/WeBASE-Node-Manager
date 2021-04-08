[rpc]
    channel_listen_ip=0.0.0.0
    jsonrpc_listen_ip=127.0.0.1
    channel_listen_port=[(${channelPort})]
    jsonrpc_listen_port=[(${jsonrpcPort})]
[p2p]
    listen_ip=0.0.0.0
    listen_port=[(${p2pPort})]
    ;enable_compress=true
    ; nodes to connect
    [# th:each="node,iter : ${nodeList}"]node.[(${iter.index})]=[(${node.frontIp})]:[(${node.p2pPort})]
    [/]

[certificate_blacklist]
    ; crl.0 should be nodeid, nodeid's length is 128
    ;crl.0=

[certificate_whitelist]
    ; cal.0 should be nodeid, nodeid's length is 128
    ;cal.0=

[group]
    group_data_path=data/
    group_config_path=conf/

[network_security]
    ; directory the certificates located in
    data_path=conf/
    ; the node private key file
    key=[(${guomi} ? 'gmnode.key' : 'node.key')]
    ; the node certificate file
    cert=[(${guomi} ? 'gmnode.crt' : 'node.crt')]
    ; the ca certificate file
    ca_cert=[(${guomi} ? 'gmca.crt' : 'ca.crt')]

[storage_security]
    enable=false
    key_manager_ip=
    key_manager_port=
    cipher_data_key=

[chain]
    id=[(${chainId})]
    ; use SM crypto or not, should nerver be changed
    sm_crypto=[(${guomi} ? 'true' : 'false')]
    sm_crypto_channel=false

[compatibility]
    ; supported_version should nerver be changed
    supported_version=[(${supportVersion})]

[log]
    enable=true
    log_path=./log
    ; enable/disable the statistics function
    enable_statistic=false
    ; network statistics interval, unit is second, default is 60s
    stat_flush_interval=60
    ; info debug trace
    level=info
    ; MB
    max_log_file_size=200
    flush=true
    log_flush_threshold=100

[flow_control]
    ; restrict QPS of the node
    ;limit_req=1000
    ; restrict the outgoing bandwidth of the node
    ; Mb, can be a decimal
    ; when the outgoing bandwidth exceeds the limit, the block synchronization operation will not proceed
    ;outgoing_bandwidth_limit=2