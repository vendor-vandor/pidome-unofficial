<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xalan="http://xml.apache.org/xalan"
                exclude-result-prefixes="xalan">
    <xsl:output omit-xml-declaration="yes"/>
    <xsl:param name="currentItemType"/>
    <xsl:param name="currentALBUM"/>
    <xsl:param name="currentALBUM_ARTIST"/>
    <xsl:param name="currentTITLE"/>
    <xsl:param name="currentTITLE_ARTIST" />
    <xsl:param name="currentDURATION" />
    <xsl:param name="playerid" />
    <xsl:param name="name" />
    <xsl:param name="location" />
    <xsl:template match="/*">
        <h3>Player</h3>
        <table id="mediaplayercontrolframe">
            <tr>
                <td valign="top" align="left" id="mediaplayercontrolframemaincontrols">
                    <table>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.HOME']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-home" value="ServerCommand.HOME"><img src="../shared/images/icons/media/home.png" alt="Home" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-home" disabled="disabled"><img src="../shared/images/icons/media/home.png" alt="Home" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.BACK']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-arrowreturn-1-w" value="ServerCommand.BACK"><img src="../shared/images/icons/media/back.png" alt="Back" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-arrowreturn-1-w" disabled="disabled"><img src="../shared/images/icons/media/back.png" alt="Back" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.OSD']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-contact" value="ServerCommand.OSD"><img src="../shared/images/icons/media/osd.png" alt="OSD" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-contact" disabled="disabled"><img src="../shared/images/icons/media/osd.png" alt="OSD" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </table>
                </td>
                <td valign="top" align="left" id="mediaplayercontrolframebrowsecontrols">
                    <table>
                        <tr>
                            <td></td>
                            <td align="center">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.UP']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-arrow-n" value="ServerCommand.UP"><img src="../shared/images/icons/media/up.png" alt="Up" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-arrow-n" disabled="disabled"><img src="../shared/images/icons/media/up.png" alt="Up" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td></td>
                        </tr>
                        <tr>
                            <td align="right">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.LEFT']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-arrow-w" value="ServerCommand.LEFT"><img src="../shared/images/icons/media/left.png" alt="Left" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-arrow-w" disabled="disabled"><img src="../shared/images/icons/media/left.png" alt="Left" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td align="center">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.CONFIRM']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-check" value="ServerCommand.CONFIRM"><img src="../shared/images/icons/media/ok.png" alt="Confirm" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled " data-icon-primary="ui-icon-circle-check" disabled="disabled"><img src="../shared/images/icons/media/ok.png" alt="Confirm" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td align="left">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.RIGHT']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-arrow-e" value="ServerCommand.RIGHT"><img src="../shared/images/icons/media/right.png" alt="Right" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-arrow-e" disabled="disabled"><img src="../shared/images/icons/media/right.png" alt="Right" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td></td>
                            <td align="center">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.DOWN']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-arrow-s" value="ServerCommand.DOWN"><img src="../shared/images/icons/media/down.png" alt="Down" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-arrow-s" disabled="disabled"><img src="../shared/images/icons/media/down.png" alt="Down" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                            <td></td>
                        </tr>
                    </table>
                </td>
                <td id="mediaplayercontrolframemediacontrols">
                    <div style="height: 100%; display: inline-block; width:100%; position: relative;">
                        <div style="width:100%; max-width: 100%; top:0px; position:absolute;">
                            <table style="width:100%; max-width: 100%;">
                                <tr>
                                    <td valign="top" style="width:30px">
                                        <strong>Title</strong>
                                    </td>
                                    <td style="width:4px;" valign="top">:</td>
                                    <td align="left" style="overflow: hidden; white-space:nowrap; vertical-align:top;" id="player_{$playerid}_title"> 
                                        <xsl:value-of select="$currentTITLE" />
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top" style="width:30px">
                                        <strong>Artist</strong>
                                    </td>
                                    <td style="width:4px;" valign="top">:</td>
                                    <td style="overflow: hidden; white-space:nowrap; vertical-align:top;" id="player_{$playerid}_artist"> 
                                        <xsl:value-of select="$currentTITLE_ARTIST" />
                                    </td>
                                </tr>
                                <tr>
                                    <td valign="top" style="width:30px">
                                        <strong>Album</strong>
                                    </td>
                                    <td style="width:4px;" valign="top">:</td>
                                    <td style="overflow: hidden; white-space:nowrap; vertical-align:top;" id="player_{$playerid}_album"> 
                                        <xsl:value-of select="$currentALBUM" />
                                    </td>
                                </tr>
                            </table>
                        </div>
                        <div style="bottom:0px; position:absolute; width:100%;">
                            <table style="width:100%;">
                                <tr>
                                    <td align="left" style="width: 10.4%; max-width: 46px;">
                                        <xsl:choose>
                                            <xsl:when test="/plugin/commandset/group[@id='playercontrols']/button[@id='PlayerCommand.PREV']">
                                                <button class="mediacontrol enabled" data-icon-primary="ui-icon-seek-first" value="PlayerCommand.PREV">
                                                    <img src="../shared/images/icons/media/seekback.png" alt="Previous" />
                                                </button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button class="mediacontrol disabled" data-icon-primary="ui-icon-seek-first">
                                                    <img src="../shared/images/icons/media/seekback.png" alt="Previous" />
                                                </button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td align="left" style="width: 10.4%; max-width: 46px;">
                                        <xsl:choose>
                                            <xsl:when test="/plugin/commandset/group[@id='playercontrols']/button[@id='PlayerCommand.STOP']">
                                                <button class="mediacontrol enabled" data-icon-primary="ui-icon-stop" value="PlayerCommand.STOP">
                                                    <img src="../shared/images/icons/media/stop.png" alt="Stop" />
                                                </button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button class="mediacontrol disabled" data-icon-primary="ui-icon-stop">
                                                    <img src="../shared/images/icons/media/stop.png" alt="Stop" />
                                                </button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td align="left" style="width: 10.4%; max-width: 46px;">
                                        <xsl:choose>
                                            <xsl:when test="/plugin/commandset/group[@id='playercontrols']/button[@id='PlayerCommand.PAUSE']">
                                                <button class="mediacontrol enabled" data-icon-primary="ui-icon-pause" value="PlayerCommand.PAUSE">
                                                    <img src="../shared/images/icons/media/pause.png" alt="Pause" />
                                                </button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button class="mediacontrol disabled" data-icon-primary="ui-icon-pause">
                                                    <img src="../shared/images/icons/media/pause.png" alt="Pause" />
                                                </button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td align="left" style="width: 10.4%; max-width: 46px;">
                                        <xsl:choose>
                                            <xsl:when test="/plugin/commandset/group[@id='playercontrols']/button[@id='PlayerCommand.PLAY']">
                                                <button class="mediacontrol enabled" data-icon-primary="ui-icon-play" value="PlayerCommand.PLAY">
                                                    <img src="../shared/images/icons/media/play.png" alt="Play" />
                                                </button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button class="mediacontrol disabled" data-icon-primary="ui-icon-play">
                                                    <img src="../shared/images/icons/media/play.png" alt="Play" />
                                                </button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td align="left" style="width: 10.4%; max-width: 46px;">
                                        <xsl:choose>
                                            <xsl:when test="/plugin/commandset/group[@id='playercontrols']/button[@id='PlayerCommand.NEXT']">
                                                <button class="mediacontrol enabled" data-icon-primary="ui-icon-seek-end" value="PlayerCommand.NEXT">
                                                    <img src="../shared/images/icons/media/seeknext.png" alt="Next" />
                                                </button>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <button class="mediacontrol disabled" data-icon-primary="ui-icon-seek-end">
                                                    <img src="../shared/images/icons/media/seeknext.png" alt="Next" />
                                                </button>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                    <td style="width: 48%;"></td>
                                </tr>
                            </table>
                        </div>
                    </div>
                </td>
                <td valign="top" align="right" id="mediaplayercontrolframevolumecontrols">
                    <table>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.VOLUP']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-triangle-n" value="ServerCommand.VOLUP"><img src="../shared/images/icons/media/volup.png" alt="Volume Up" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-triangle-n"><img src="../shared/images/icons/media/volup.png" alt="Volume Up" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.MUTE']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-volume-off" value="ServerCommand.MUTE"><img src="../shared/images/icons/media/unmuted.png" alt="Mute/Unmute" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-volume-off"><img src="../shared/images/icons/media/unmuted.png" alt="Mute/Unmute" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                        <tr>
                            <td valign="top">
                                <xsl:choose>
                                    <xsl:when test="/plugin/commandset/group[@id='controls']/button[@id='ServerCommand.VOLDOWN']">
                                        <button class="mediacontrol enabled" data-icon-primary="ui-icon-circle-triangle-s" value="ServerCommand.VOLDOWN"><img src="../shared/images/icons/media/voldown.png" alt="Volume Down" /></button>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <button class="mediacontrol disabled" data-icon-primary="ui-icon-circle-triangle-s"><img src="../shared/images/icons/media/voldown.png" alt="Volume Down" /></button>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
        </table>
    </xsl:template>
</xsl:stylesheet>