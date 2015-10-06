/*
 * Copyright 2014 CyberVision, Inc.
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

package org.kaaproject.kaa.server.admin.client.mvp.data;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.kaaproject.avro.ui.gwt.client.widget.grid.AbstractGrid;
import org.kaaproject.kaa.common.dto.EndpointProfileDto;
import org.kaaproject.kaa.server.admin.client.KaaAdmin;
import org.kaaproject.kaa.server.admin.client.mvp.activity.grid.AbstractDataProvider;
import org.kaaproject.kaa.server.admin.client.util.HasErrorMessage;

import java.util.List;

public class EndpointProfilesDataProvider extends AbstractDataProvider<EndpointProfileDto> {

    private String groupID = "";
    private String limit = "10";
    private String offset = "0";


    public EndpointProfilesDataProvider(AbstractGrid<EndpointProfileDto, ?> dataGrid,
                                        HasErrorMessage hasErrorMessage,
                                        String groupID) {
        super(dataGrid, hasErrorMessage, false);
        this.groupID = groupID;
        addDataDisplay();
    }

    @Override
    protected void loadData(final LoadCallback callback) {
        KaaAdmin.getDataSource().getEndpointProfileByGroupID(groupID, limit, offset,
                new AsyncCallback<List<EndpointProfileDto>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        callback.onFailure(caught);
                    }

                    @Override
                    public void onSuccess(List<EndpointProfileDto> result) {
                        callback.onSuccess(result);
                    }
                });
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public void setLimit(String limit) {
        this.limit = limit;
    }

    public void setOffset(String offset) {
        this.offset = offset;
    }
}
