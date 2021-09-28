/**
 * Copyright 2014-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.tools.pagetools;

import com.webank.webase.node.mgr.tools.pagetools.entity.MapHandle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * transfer map to paged list through PageData
 * @param <T>
 */
public class Map2PagedList<T> {
    private List<MapHandle> data;
    private Integer pageSize;
    private Integer pageIndex;

    /**
     *  map constructor
     * @param maps
     * @param pageSize
     * @param pageIndex
     */
    public Map2PagedList(Map<T,T> maps, Integer pageSize, Integer pageIndex) {
        List<MapHandle> mapList = new ArrayList<>();
        Iterator it = maps.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next().toString();
            MapHandle handle = new MapHandle(key, maps.get(key));
            mapList.add(handle);
        }
        this.data = mapList;
        this.pageSize = pageSize;
        this.pageIndex = pageIndex;
    }

    public List<MapHandle> getPagedList() {
        // 结果记录列表
        List<MapHandle> resList = null;
        // 总记录数
        Integer size = data.size();
        // 总页数
        Integer pages = size / pageSize;
        if (size - pages * pageSize > 0) {
            ++pages;
        }
        if (pageIndex < pages) {
            resList = data.subList((pageIndex - 1) * pageSize, pageSize * pageIndex);
        } else if (pageIndex.equals(pages)) {
            resList = data.subList((pageIndex - 1) * pageSize, size);
        } else {
            resList = new ArrayList<>();
        }
        PageData<MapHandle> list = new PageData<>(resList, pageIndex, pageSize, resList.size(), size, pages);
        return list.getList();
    }
}
