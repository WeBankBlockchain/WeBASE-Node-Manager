[consensus]
    ; consensus algorithm now support PBFT(consensus_type=pbft), Raft(consensus_type=raft)
    ; and rpbft(consensus_type=rpbft)
    consensus_type=pbft
    ; the max number of transactions of a block
    max_trans_num=1000
    ; rpbft related configuration
    ; the sealers num of each consensus epoch
    epoch_sealer_num=[(${sealerCount})]
    ; the number of generated blocks each epoch
    epoch_block_num=1000
    ; the node id of consensusers
    [# th:each="nodeId,iter : ${nodeIdList}"]node.[(${iter.index})]=[(${nodeId})]
    [/]

[state]
    type=storage
[tx]
    ; transaction gas limit
    gas_limit=300000000
[group]
    id=[(${groupId})]
    timestamp=[(${timestamp})]
[evm]
    enable_free_storage=false
