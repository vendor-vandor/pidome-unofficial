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
    <xsl:variable name="uid" select="uuid:randUUIDNoDashes()"/>
    <xsl:template match="/*">
                <tr id="row_{$uid}_{$device_id}">
                    <td>
                        <xsl:value-of select="$locationname" />
                    </td>
                    <td>
                        <xsl:value-of select="$devicedbname" /> (<xsl:value-of select="/device/name" />)
                        <input type="hidden" name="macroDeviceDeviceId_{$uid}_{$device_id}" id="macroDeviceDeviceId_{$uid}_{$device_id}" value="{$device_id}" />
                    </td>
                    <td>
                        <select name="macroDeviceGroup_{$uid}_{$device_id}" id="macroDeviceGroup_{$uid}_{$device_id}" onchange="setSelectGroup{$uid}_{$device_id}(this);">
                            <option value="">Make selection below:</option>
                            <xsl:for-each select="/device/commandset/group">
                                <xsl:if test="@id!='location' and @id!='options'">
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
                            </xsl:for-each>
                        </select>
                    </td>
                    <td id="selectedGroupFiller{$uid}_{$device_id}"></td>
                    <td id="selectedSetFiller{$uid}_{$device_id}"></td>
                    <td id="selectedDeleteDevice{$uid}_{$device_id}"><button class="deleteDevice" id="deleteDevice_{$uid}_{$device_id}" name="deleteDevice_{$uid}_{$device_id}">Remove device</button></td>
                </tr>
        <script type="text/javascript">
            
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
                                $("#selectedGroupFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<select name="macroDeviceSet_{$uid}_{$device_id}" id="macroDeviceSet_{$uid}_{$device_id}" onchange="setSelectSet{$uid}_{$device_id}(this);">
                                    <option value="-">Make selection below</option>
                                    <xsl:for-each select="toggle">
                                        <xsl:if test="@id=$set">
                                            <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$set">
                                            <option value="{@id}"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                    </xsl:for-each>
                                    <xsl:for-each select="select">
                                        <xsl:if test="@id=$set">
                                            <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$set">
                                            <option value="{@id}"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                    </xsl:for-each>
                                    <xsl:for-each select="slider">
                                        <xsl:if test="@id=$set">
                                            <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$set">
                                            <option value="{@id}"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                    </xsl:for-each>
                                    <xsl:for-each select="button">
                                        <xsl:if test="@id=$set">
                                            <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                        <xsl:if test="@id!=$set">
                                            <option value="{@id}"><xsl:value-of select="@label" /></option>
                                        </xsl:if>
                                    </xsl:for-each>
                                    <xsl:for-each select="control">
                                        <xsl:choose>
                                            <xsl:when test="@type='colorpicker'">
                                                <xsl:if test="@id=$set">
                                                    <option value="{@id}" selected="selected"><xsl:value-of select="@label" /></option>
                                                </xsl:if>
                                                <xsl:if test="@id!=$set">
                                                    <option value="{@id}"><xsl:value-of select="@label" /></option>
                                                </xsl:if>
                                            </xsl:when>
                                        </xsl:choose>
                                    </xsl:for-each>
                                </select>');
                            break;
                        </xsl:if>
                    </xsl:for-each>
                }
            }
            
            function setSelectSet<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(sel){
                var value = sel.options[sel.selectedIndex].value;
                $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").empty();
                setSet<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(value);
            }
            function setSet<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />(value){
                switch(value){
                    <xsl:for-each select="/device/commandset/group">
                        <xsl:param name="cmdGroupIdParam" select="@id" />
                        <xsl:if test="@id!='location'">
                            <xsl:for-each select="select">
                                <xsl:param name="deviceSelectParam" select="@id" />
                                case '<xsl:value-of select="$deviceSelectParam" />':
                                $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<select name="command_{$uid}_{$device_id}" id="command_{$uid}_{$device_id}">
                                    <xsl:for-each select="option">
                                        <xsl:choose>
                                            <xsl:when test="$command=@value">
                                                <option value="{@value}" selected="selected">text()</option>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <option value="{@value}">text()</option>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:for-each>
                                </select>
                            </xsl:for-each>
                            <xsl:for-each select="toggle">
                                <xsl:param name="deviceSetParam" select="@id" />
                                case '<xsl:value-of select="$deviceSetParam" />':
                                    $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<select name="command_{$uid}_{$device_id}" id="command_{$uid}_{$device_id}">
                                        <xsl:for-each select="on">
                                            <xsl:choose>
                                                <xsl:when test="$command=@value">
                                                    <option value="{@value}" selected="selected">On</option>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <option value="{@value}">On</option>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                        <xsl:for-each select="off">
                                            <xsl:choose>
                                                <xsl:when test="$command=@value">
                                                    <option value="{@value}" selected="selected">Off</option>
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <option value="{@value}">Off</option>
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:for-each>
                                    </select>');
                                break;
                            </xsl:for-each>
                            <xsl:for-each select="slider">
                                <xsl:param name="deviceSliderParam" select="@value" />
                                case '<xsl:value-of select="$deviceSliderParam" />':
                                    $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<div id="{$uid}_{$deviceSliderParam}"></div><input type="text" name="command_{$uid}_{$device_id}" id="command_{$uid}_{$device_id}" style="border: 0; color: #f6931f; font-weight: bold;" />');
                                     createMacroslider('<xsl:value-of select="$uid"/>_<xsl:value-of select="$deviceSliderParam" />', 'command_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />', <xsl:value-of select="@min" />, <xsl:value-of select="@max" />, '<xsl:value-of select="$command" />');  
                                break;
                            </xsl:for-each>
                            <xsl:for-each select="button">
                                <xsl:param name="buttonParam" select="@id" />
                                case '<xsl:value-of select="$buttonParam" />':
                                    $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<div id="{$uid}_{$buttonParam}"></div><input type="text" name="command_{$uid}_{$device_id}" id="command_{$uid}_{$device_id}" style="border: 0; font-weight: bold; background: transparent;" value="{@value}" readonly="readonly" />');
                                break;
                            </xsl:for-each>
                            <xsl:for-each select="control">
                                <xsl:choose>
                                    <xsl:when test="@type='colorpicker'">
                                        <xsl:param name="deviceColorParam" select="@id" />
                                        case '<xsl:value-of select="$deviceColorParam" />':
                                            $("#selectedSetFiller<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />").append('<xsl:choose>
                                                <xsl:when test="button">
                                                    <select id="endCommand_{$uid}_{$device_id}" name="endCommand_{$uid}_{$device_id}">
                                                        <xsl:for-each select="button">
                                                            <option id="colorpicker-{@value}" name="colorpicker-{@value}" value="{@value}"><xsl:value-of select="text()" /></option>
                                                        </xsl:for-each>
                                                    </select>
                                                </xsl:when>
                                            </xsl:choose>
                                            <input class="minicolors inline" id="{$uid}_{$deviceColorParam}" type="text" value=""/>
                                            <input type="text" name="command_{$uid}_{$device_id}" id="command_{$uid}_{$device_id}" style="display:none;" value=""/>');
                                             createColorPicker('<xsl:value-of select="$uid"/>_<xsl:value-of select="$deviceColorParam" />' ,'command_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />', 'endCommand_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />','<xsl:value-of select="$command" />');  
                                        break;
                                    </xsl:when>
                                </xsl:choose>
                            </xsl:for-each>
                            
                        </xsl:if>
                    </xsl:for-each>
                }
            }
            setGroup<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />('<xsl:value-of select="$group" />');
            setSet<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />('<xsl:value-of select="$set" />');
            $( "#deleteDevice_<xsl:value-of select="$uid"/>_<xsl:value-of select="$device_id" />" ).button({icons: { primary: "ui-icon-cancel" }})
            .click(function() {
                removeDeviceFromMacro($(this).attr("id"));
                return false;
            });
        </script>
        
    </xsl:template>
</xsl:stylesheet>