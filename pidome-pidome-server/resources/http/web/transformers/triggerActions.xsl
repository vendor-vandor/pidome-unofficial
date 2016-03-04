<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xalan="http://xml.apache.org/xalan"
                xmlns:devVals="xalan://org.pidome.server.system.webservice.webclient.helpers.DeviceValuesParser" 
                xmlns:uuid="xalan://org.pidome.server.system.webservice.webclient.helpers.WebclientHelpers"
                extension-element-prefixes="devVals" exclude-result-prefixes="xalan devVals uuid">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:param name="locationname" />
    <xsl:param name="devicedbname" />
    <xsl:param name="group" />
    <xsl:param name="set" />
    <xsl:param name="command" />
    <xsl:param name="device_id" />
    <xsl:param name="uid" />
    <xsl:param name="method" />
    <xsl:param name="valueType" />
    <xsl:param name="value" />
    <xsl:param name="ruleid" />
    <xsl:template match="/*">
                <tr id="triggerRow_{$ruleid}_subject_{$uid}">
                    <td>
                        <xsl:value-of select="$locationname" />
                    </td>
                    <td>
                        <xsl:value-of select="$devicedbname" /> (<xsl:value-of select="/device/name" />)
                        <input type="hidden" name="triggerDeviceDeviceId_{$ruleid}_{$uid}_{$device_id}" id="triggerDeviceDeviceId_{$ruleid}_{$uid}_{$device_id}" value="{$device_id}" />
                    </td>
                    <td>
                        <select name="triggerDeviceGroup_{$ruleid}_{$uid}_{$device_id}" id="triggerDeviceGroup_{$ruleid}_{$uid}_{$device_id}" onchange="setSelectGroup{$uid}_{$device_id}(this);">
                            <option value="">Make selection below:</option>
                            <xsl:for-each select="/device/commandset/group">
                                <xsl:if test="@id!='location' and @id!='options'">
                                    <xsl:if test="child::data">
                                        <xsl:if test="@id=$group">
                                            <option value="{@id}" selected="selected">
                                                <xsl:value-of select="@label" />
                                            </option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$group">
                                            <option value="{@id}">
                                                <xsl:value-of select="@label" />
                                            </option>
                                        </xsl:if>
                                    </xsl:if>
                                </xsl:if>
                            </xsl:for-each>
                        </select>
                    </td>
                    <td id="selectedGroupFiller{$uid}_{$device_id}"></td>
                    <td id="selectedSetFiller{$uid}_{$device_id}"></td>
                    <td>
                        <select name="triggerType_{$ruleid}_{$uid}_{$device_id}" id="triggerType_{$ruleid}_{$uid}_{$device_id}">
                            <xsl:if test="$method='EQUALS'">
                                <option value="EQUALS" selected="selected">equals to</option>
                            </xsl:if>
                            <xsl:if test="$method!='EQUALS'">
                                <option value="EQUALS">equals to</option>
                            </xsl:if>
                            <xsl:if test="$method='DIFFER'">
                                <option value="DIFFER" selected="selected">differs from</option>
                            </xsl:if>
                            <xsl:if test="$method!='DIFFER'">
                                <option value="DIFFER">differs from</option>
                            </xsl:if>
                            <xsl:if test="$method='LESSTHEN'">
                                <option value="LESSTHEN" selected="selected">less then</option>
                            </xsl:if>
                            <xsl:if test="$method!='LESSTHEN'">
                                <option value="LESSTHEN">less then</option>
                            </xsl:if>
                            <xsl:if test="$method='GREATERTHEN'">
                                <option value="GREATERTHEN" selected="selected">greater then</option>
                            </xsl:if>
                            <xsl:if test="$method!='GREATERTHEN'">
                                <option value="GREATERTHEN">greater then</option>
                            </xsl:if>
                        </select>
                    </td>
                    <td>
                        <input type="text" class="numeric" name="triggerValue_{$ruleid}_{$uid}_{$device_id}" id="triggerValue_{$ruleid}_{$uid}_{$device_id}" value="{$value}" size="10" />
                        <input type="text" name="triggerValueType_{$ruleid}_{$uid}_{$device_id}" id="triggerValueType_{$ruleid}_{$uid}_{$device_id}" value="double" style="display:none;" />
                    </td>
                    <td>
                        <button value="triggerRow_{$ruleid}_subject_{$uid}" class="removeSubject" id="triggerRow_{$ruleid}_subject_{$uid}_remove" name="removeSubject">Remove subject</button>
                    </td>
                </tr>
                <script>
                    $( "#triggerRow_<xsl:value-of select="$ruleid" />_subject_<xsl:value-of select="$uid" />_remove" ).button({icons: { primary: "ui-icon-cancel" }}).click(function() {
                        $( "#" + $(this).val() ).remove();
                        return false;
                    });
                </script>
        <script type="text/javascript">
            ensureNumeric("#triggerValue_<xsl:value-of select="$ruleid"/>_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />");
            function setSelectGroup<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(sel){
                var value = sel.options[sel.selectedIndex].value;
                $("#selectedGroupFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").empty();
                $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").empty();
                setGroup<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(value);
            }
            function setGroup<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(value){
                switch(value){
                    <xsl:for-each select="/device/commandset/group">
                        <xsl:param name="cmdGroupIdParam" select="@id" />
                        <xsl:if test="@id!='location'">
                            case '<xsl:value-of select="$cmdGroupIdParam" />':
                                $("#selectedGroupFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<select name="triggerDeviceSet_{$ruleid}_{$uid}_{$device_id}" id="triggerDeviceSet_{$ruleid}_{$uid}_{$device_id}" onchange="setSelectSet{$uid}_{$device_id}(this);">
                                    <option value="-">Make selection below</option>
                                    <xsl:for-each select="data">
                                        <xsl:if test="@id=$set">
                                            <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$set">
                                            <option value="{@id}"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                    </xsl:for-each>
                                </select>');
                            break;
                        </xsl:if>
                    </xsl:for-each>
                }
            }
            
            function setSelectSet<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(sel){
                var value = sel.options[sel.selectedIndex].value;
            }
             setGroup<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />('<xsl:value-of select="$group" />');
        </script>
        
    </xsl:template>
</xsl:stylesheet>