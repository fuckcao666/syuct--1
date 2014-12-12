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

package org.kaaproject.kaa.server.admin.client.mvp.view.appender;

import static org.kaaproject.kaa.server.admin.client.util.Utils.isNotBlank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kaaproject.kaa.common.dto.logs.LogHeaderStructureDto;
import org.kaaproject.kaa.server.admin.client.mvp.view.LogAppenderView;
import org.kaaproject.kaa.server.admin.client.mvp.view.base.BaseDetailsViewImpl;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.AppenderInfoListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.IntegerListBox;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminRecordFieldWidget;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextArea;
import org.kaaproject.kaa.server.admin.client.mvp.view.widget.KaaAdminSizedTextBox;
import org.kaaproject.kaa.server.admin.client.util.Utils;
import org.kaaproject.kaa.server.admin.shared.logs.LogAppenderInfoDto;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget.RecordFieldWidget;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget.SizedTextArea;
import org.kaaproject.kaa.server.common.avro.ui.gwt.client.widget.SizedTextBox;
import org.kaaproject.kaa.server.common.avro.ui.shared.RecordField;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.watopi.chosen.client.event.ChosenChangeEvent;
import com.watopi.chosen.client.event.ChosenChangeEvent.ChosenChangeHandler;
import com.watopi.chosen.client.gwt.ChosenListBox;

public class LogAppenderViewImpl extends BaseDetailsViewImpl implements LogAppenderView,                                                                        
                                                                        ValueChangeHandler<RecordField>,
                                                                        ChosenChangeHandler {

    private static final String REQUIRED = "required";

    private SizedTextBox name;
    private IntegerListBox minSchemaVersion;
    private IntegerListBox maxSchemaVersion;
    private CheckBox confirmDelivery;
    private AppenderInfoListBox appenderInfo;
    private SizedTextArea description;
    private SizedTextBox createdUsername;
    private SizedTextBox createdDateTime;
    private ChosenListBox metadatalistBox;
    private RecordFieldWidget configuration;
    
    private List<Integer> schemaVersions;
    
    private static final String FULL_WIDTH = "100%";

    public LogAppenderViewImpl(boolean create) {
        super(create);
    }

    @Override
    protected void initDetailsTable() {
        Label authorLabel = new Label(Utils.constants.author());
        createdUsername = new KaaAdminSizedTextBox(-1, false);
        createdUsername.setWidth(FULL_WIDTH);
        int idx = 0;
        detailsTable.setWidget(idx, 0, authorLabel);
        detailsTable.setWidget(idx, 1, createdUsername);

        authorLabel.setVisible(!create);
        createdUsername.setVisible(!create);

        Label dateTimeCreatedLabel = new Label(Utils.constants.dateTimeCreated());
        createdDateTime = new KaaAdminSizedTextBox(-1, false);
        createdDateTime.setWidth(FULL_WIDTH);
        
        idx++;        
        detailsTable.setWidget(idx, 0, dateTimeCreatedLabel);
        detailsTable.setWidget(idx, 1, createdDateTime);

        dateTimeCreatedLabel.setVisible(!create);
        createdDateTime.setVisible(!create);

        name = new KaaAdminSizedTextBox(DEFAULT_TEXTBOX_SIZE);
        name.setWidth(FULL_WIDTH);
        Label nameLabel = new Label(Utils.constants.name());
        nameLabel.addStyleName(REQUIRED);
        idx++;
        detailsTable.setWidget(idx, 0, nameLabel);
        detailsTable.setWidget(idx, 1, name);
        name.addInputHandler(this);

        Label minSchemaVersionLabel = new Label(Utils.constants.minVersion());
        minSchemaVersionLabel.addStyleName(REQUIRED);
        minSchemaVersion = new IntegerListBox();
        minSchemaVersion.setWidth("30%");
        
        minSchemaVersion.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                updateMaxSchemaVersions();
                fireChanged();
            }
        });
        
        idx++;
        detailsTable.setWidget(idx, 0, minSchemaVersionLabel);
        detailsTable.setWidget(idx, 1, minSchemaVersion);

        Label maxSchemaVersionLabel = new Label(Utils.constants.maxVersion());
        maxSchemaVersionLabel.addStyleName(REQUIRED);
        maxSchemaVersion = new IntegerListBox();
        maxSchemaVersion.setWidth("30%");
        
        maxSchemaVersion.addValueChangeHandler(new ValueChangeHandler<Integer>() {
            @Override
            public void onValueChange(ValueChangeEvent<Integer> event) {
                fireChanged();
            }
        });

        idx++;
        detailsTable.setWidget(idx, 0, maxSchemaVersionLabel);
        detailsTable.setWidget(idx, 1, maxSchemaVersion);
        
        confirmDelivery = new CheckBox();
        confirmDelivery.setWidth("100%");
        Label confirmDeliveryLabel = new Label(Utils.constants.mandatory());
        idx++;
        detailsTable.setWidget(idx, 0, confirmDeliveryLabel);
        detailsTable.setWidget(idx, 1, confirmDelivery);
        confirmDelivery.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                fireChanged();
            }
        });

        Label logMetadata = new Label(Utils.constants.logMetada());
        generateMetadataListBox();
        idx++;
        detailsTable.setWidget(idx, 0, logMetadata);
        detailsTable.setWidget(idx, 1, metadatalistBox);

        description = new KaaAdminSizedTextArea(1024);
        description.setWidth(FULL_WIDTH);
        description.getTextArea().getElement().getStyle().setPropertyPx("minHeight", 100);
        Label descriptionLabel = new Label(Utils.constants.description());
        idx++;
        detailsTable.setWidget(idx, 0, descriptionLabel);
        detailsTable.setWidget(idx, 1, description);
        detailsTable.getCellFormatter().setVerticalAlignment(6, 0, HasVerticalAlignment.ALIGN_TOP);
        description.addInputHandler(this);

        Label typeLabel = new Label(Utils.constants.logAppenderType());
        appenderInfo = new AppenderInfoListBox();
        appenderInfo.setEnabled(create);
        appenderInfo.addValueChangeHandler(new ValueChangeHandler<LogAppenderInfoDto>() {
            @Override
            public void onValueChange(ValueChangeEvent<LogAppenderInfoDto> event) {
                updateAppender(event.getValue());
            }
        });

        idx++;
        detailsTable.setWidget(idx, 0, typeLabel);
        detailsTable.setWidget(idx, 1, appenderInfo);

        getFooter().setStyleName("b-app-content-details-table");
        
        configuration = new KaaAdminRecordFieldWidget();
        configuration.addValueChangeHandler(this);
        getFooter().add(configuration);
        
        name.setFocus(true);
    }

    @Override
    protected String getCreateTitle() {
        return Utils.constants.addLogAppender();
    }

    @Override
    protected String getViewTitle() {
        return Utils.constants.logAppender();
    }

    @Override
    protected String getSubTitle() {
        return Utils.constants.logAppenderDetails();
    }
    
	@Override
	public HasValue<Boolean> getConfirmDelivery() {
		return confirmDelivery;
	}    

    @Override
    protected void resetImpl() {
        name.setValue("");
        minSchemaVersion.reset();
        maxSchemaVersion.reset();
        description.setValue("");
        confirmDelivery.setValue(true);
        createdUsername.setValue("");
        createdDateTime.setValue("");
        if (metadatalistBox != null) {
            generateMetadataListBox();
        }
        if (appenderInfo != null) {
            appenderInfo.setValue(null, true);
        }
    }

    @Override
    protected boolean validate() {
        boolean result = isNotBlank(name.getValue());
        result &= minSchemaVersion.getValue() != null;
        result &= maxSchemaVersion.getValue() != null;
        result &= configuration.validate();
        return result;
    }

    @Override
    public ValueListBox<Integer> getMinSchemaVersion() {
        return minSchemaVersion;
    }

    @Override
    public ValueListBox<Integer> getMaxSchemaVersion() {
        return maxSchemaVersion;
    }
    
    @Override
    public HasValue<String> getName() {
        return name;
    }

    @Override
    public ValueListBox<LogAppenderInfoDto> getAppenderInfo() {
        return appenderInfo;
    }

    @Override
    public HasValue<String> getDescription() {
        return description;
    }

    @Override
    public HasValue<String> getCreatedUsername() {
        return createdUsername;
    }

    @Override
    public HasValue<String> getCreatedDateTime() {
        return createdDateTime;
    }

    @Override
    public HasValue<RecordField> getConfiguration() {
        return configuration;
    }
    
    private void updateAppender(LogAppenderInfoDto value) {
        configuration.setValue(value != null ? value.getConfigForm() : null);
        fireChanged();
    }

    private void generateMetadataListBox() {
        if (metadatalistBox != null) {
            metadatalistBox.clear();
        } else {
            metadatalistBox = new ChosenListBox(true);
            metadatalistBox.addChosenChangeHandler(this);
        }
        metadatalistBox.setPixelSize(300, 30);
        metadatalistBox.setPlaceholderText("Select metadata components");
        metadatalistBox.addItem(LogHeaderStructureDto.KEYHASH.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.TIMESTAMP.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.TOKEN.getValue());
        metadatalistBox.addItem(LogHeaderStructureDto.VERSION.getValue());
    }

    public void setMetadataListBox(List<LogHeaderStructureDto> header) {
        if (header != null) {
            for (LogHeaderStructureDto field : header) {
                metadatalistBox.setSelectedValue(field.getValue());
            }
        }
    }

    public List<LogHeaderStructureDto> getHeader() {
        List<LogHeaderStructureDto> header = Collections.emptyList();
        if (metadatalistBox != null) {
            String[] selected = metadatalistBox.getValues();
            if (selected != null && selected.length != 0) {
                header = new ArrayList<>();
                for (String field : selected) {
                    for (LogHeaderStructureDto value : LogHeaderStructureDto.values()) {
                        if (value.getValue().equalsIgnoreCase(field)) {
                            header.add(value);
                            continue;
                        }
                    }
                }
            }
        }
        return header;
    }
    
    private void updateMaxSchemaVersions() {
        if (schemaVersions != null) {
           Integer minVersionValue = minSchemaVersion.getValue();
           List<Integer> maxSchemaVersions = null;
           Integer maxVersionValue = maxSchemaVersion.getValue();
           
           if (minVersionValue != null) {
               maxSchemaVersions = new ArrayList<>();
               for (Integer version : schemaVersions) {
                   if (version >= minVersionValue) {
                       maxSchemaVersions.add(version);
                   }
               }
               if (maxVersionValue != null && maxVersionValue < minVersionValue) {
                   maxVersionValue = minVersionValue;
               }
           }
           else {
               maxSchemaVersions = new ArrayList<>(schemaVersions);
           }
           maxSchemaVersions.add(Integer.MAX_VALUE);
           if (maxVersionValue == null) {
               maxVersionValue = Integer.MAX_VALUE;
           }
           maxSchemaVersion.setValue(maxVersionValue);
           maxSchemaVersion.setAcceptableValues(maxSchemaVersions);
        }
    }
    
    @Override
    public void setSchemaVersions(List<Integer> schemaVersions) {
        this.schemaVersions = schemaVersions;
        if (minSchemaVersion.getValue() == null && !schemaVersions.isEmpty()) {
            minSchemaVersion.setValue(schemaVersions.get(0));
        }
        minSchemaVersion.setAcceptableValues(schemaVersions);
        updateMaxSchemaVersions();
    }

    @Override
    public void onChange(ChosenChangeEvent event) {
        fireChanged();
    }

    @Override
    public void onValueChange(ValueChangeEvent<RecordField> event) {
        fireChanged();
    }
}
