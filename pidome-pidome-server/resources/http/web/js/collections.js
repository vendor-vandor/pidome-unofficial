/*
 * jQuery UI Menubar @VERSION
 *
 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
 * Dual licensed under the MIT or GPL Version 2 licenses.
 * http://jquery.org/license
 *
 * http://docs.jquery.com/UI/Menubar
 *
 * Depends:
 *	jquery.ui.core.js
 *	jquery.ui.widget.js
 *	jquery.ui.position.js
 *	jquery.ui.menu.js
 */
(function($) {

// TODO when mixing clicking menus and keyboard navigation, focus handling is broken
// there has to be just one item that has tabindex
    $.widget("ui.menubar", {
        version: "@VERSION",
        options: {
            autoExpand: false,
            buttons: false,
            items: "li",
            menuElement: "ul",
            menuIcon: false,
            position: {
                my: "left top",
                at: "left bottom"
            }
        },
        _create: function() {
            // Top-level elements containing the submenu-triggering elem
            this.menuItems = this.element.children(this.options.items);
            // Links or buttons in menuItems, triggers of the submenus
            this.items = [];

            this._initializeMenubarsBoundElement();
            this._initializeWidget();
            this._initializeMenuItems();

            // Keep track of open submenus
            this.openSubmenus = 0;
        },
        _initializeMenubarsBoundElement: function() {
            this.element
                    .addClass("ui-menubar ui-widget-header ui-helper-clearfix")
                    .attr("role", "menubar");
        },
        _initializeWidget: function() {
            var menubar = this;

            this._on({
                keydown: function(event) {
                    if (event.keyCode === $.ui.keyCode.ESCAPE && menubar.active && menubar.active.menu("collapse", event) !== true) {
                        var active = menubar.active;
                        menubar.active.blur();
                        menubar._close(event);
                        $(event.target).blur().mouseleave();
                        active.prev().focus();
                    }
                },
                focusin: function(event) {
                    clearTimeout(menubar.closeTimer);
                },
                focusout: function(event) {
                    menubar.closeTimer = setTimeout(function() {
                        menubar._close(event);
                    }, 150);
                },
                "mouseleave .ui-menubar-item": function(event) {
                    if (menubar.options.autoExpand) {
                        menubar.closeTimer = setTimeout(function() {
                            menubar._close(event);
                        }, 150);
                    }
                },
                "mouseenter .ui-menubar-item": function(event) {
                    clearTimeout(menubar.closeTimer);
                }
            });
        },
        _initializeMenuItems: function() {
            var $item,
                    menubar = this;

            this.menuItems
                    .addClass("ui-menubar-item")
                    .attr("role", "presentation");

            $.each(this.menuItems, function(index, menuItem) {
                menubar._initializeMenuItem($(menuItem), menubar);
                menubar._identifyMenuItemsNeighbors($(menuItem), menubar, index);
            });
        },
        _identifyMenuItemsNeighbors: function($menuItem, menubar, index) {
            var collectionLength = this.menuItems.toArray().length,
                    isFirstElement = (index === 0),
                    isLastElement = (index === (collectionLength - 1));

            if (isFirstElement) {
                $menuItem.data("prevMenuItem", $(this.menuItems[collectionLength - 1]));
                $menuItem.data("nextMenuItem", $(this.menuItems[index + 1]));
            } else if (isLastElement) {
                $menuItem.data("nextMenuItem", $(this.menuItems[0]));
                $menuItem.data("prevMenuItem", $(this.menuItems[index - 1]));
            } else {
                $menuItem.data("nextMenuItem", $(this.menuItems[index + 1]));
                $menuItem.data("prevMenuItem", $(this.menuItems[index - 1]));
            }
        },
        _initializeMenuItem: function($menuItem, menubar) {
            var $item = $menuItem.children("button, a");

            menubar._determineSubmenuStatus($menuItem, menubar);
            menubar._styleMenuItem($menuItem, menubar);

            if ($menuItem.data("hasSubMenu")) {
                menubar._initializeSubMenu($menuItem, menubar);
            }

            $item.data("parentMenuItem", $menuItem);
            menubar.items.push($item);
            menubar._initializeItem($item, menubar);
        },
        _determineSubmenuStatus: function($menuItem, menubar) {
            var subMenus = $menuItem.children(menubar.options.menuElement),
                    hasSubMenu = subMenus.length > 0;
            $menuItem.data("hasSubMenu", hasSubMenu);
        },
        _styleMenuItem: function($menuItem, menubar) {
            $menuItem.css({
                "border-width": "1px",
                "border-style": "hidden"
            });
        },
        _initializeSubMenu: function($menuItem, menubar) {
            var subMenus = $menuItem.children(menubar.options.menuElement);

            subMenus
                    .menu({
                position: {
                    within: this.options.position.within
                },
                select: function(event, ui) {
                    ui.item.parents("ul.ui-menu:last").hide();
                    menubar._close();
                    // TODO what is this targetting? there's probably a better way to access it
                    $(event.target).prev().focus();
                    menubar._trigger("select", event, ui);
                },
                menus: this.options.menuElement
            })
                    .hide()
                    .attr({
                "aria-hidden": "true",
                "aria-expanded": "false"
            });

            this._on(subMenus, {
                keydown: function(event) {
                    var parentButton,
                            menu = $(this);
                    if (menu.is(":hidden")) {
                        return;
                    }
                    switch (event.keyCode) {
                        case $.ui.keyCode.LEFT:
                            parentButton = menubar.active.prev(".ui-button");

                            if (parentButton.parent().prev().data('hasSubMenu')) {
                                menubar.active.blur();
                                menubar._open(event, parentButton.parent().prev().find(".ui-menu"));
                            } else {
                                parentButton.parent().prev().find(".ui-button").focus();
                                menubar._close(event);
                                this.open = true;
                            }

                            event.preventDefault();
                            break;
                        case $.ui.keyCode.RIGHT:
                            this.next(event);
                            event.preventDefault();
                            break;
                    }
                },
                focusout: function(event) {
                    event.stopImmediatePropagation();
                }
            });
        },
        _initializeItem: function($anItem, menubar) {
            //only the first item is eligible to receive the focus
            var menuItemHasSubMenu = $anItem.data("parentMenuItem").data("hasSubMenu");

            // Only the first item is tab-able
            if (menubar.items.length === 1) {
                $anItem.attr("tabindex", 1);
            } else {
                $anItem.attr("tabIndex", -1);
            }

            this._focusable(this.items);
            this._hoverable(this.items);
            this._applyDOMPropertiesOnItem($anItem, menubar);

            this.__applyMouseAndKeyboardBehaviorForMenuItem($anItem, menubar);

            if (menuItemHasSubMenu) {
                this.__applyMouseBehaviorForSubmenuHavingMenuItem($anItem, menubar);
                this.__applyKeyboardBehaviorForSubmenuHavingMenuItem($anItem, menubar);

                $anItem.attr("aria-haspopup", "true");
                if (menubar.options.menuIcon) {
                    $anItem.addClass("ui-state-default").append("<span class='ui-button-icon-secondary ui-icon ui-icon-triangle-1-s'></span>");
                    $anItem.removeClass("ui-button-text-only").addClass("ui-button-text-icon-secondary");
                }
            } else {
                this.__applyMouseBehaviorForSubmenulessMenuItem($anItem, menubar);
                this.__applyKeyboardBehaviorForSubmenulessMenuItem($anItem, menubar);
            }
        },
        __applyMouseAndKeyboardBehaviorForMenuItem: function($anItem, menubar) {
            menubar._on($anItem, {
                focus: function(event) {
                    $anItem.addClass("ui-state-focus");
                },
                focusout: function(event) {
                    $anItem.removeClass("ui-state-focus");
                }
            });
        },
        _applyDOMPropertiesOnItem: function($item, menubar) {
            $item
                    .addClass("ui-button ui-widget ui-button-text-only ui-menubar-link")
                    .attr("role", "menuitem")
                    .wrapInner("<span class='ui-button-text'></span>");

            if (menubar.options.buttons) {
                $item.removeClass("ui-menubar-link").addClass("ui-state-default");
            }
        },
        __applyMouseBehaviorForSubmenuHavingMenuItem: function(input, menubar) {
            var menu = input.next(menubar.options.menuElement),
                    mouseBehaviorCallback = function(event) {
                // ignore triggered focus event
                if (event.type === "focus" && !event.originalEvent) {
                    return;
                }
                event.preventDefault();
                // TODO can we simplify or extract this check? especially the last two expressions
                // there's a similar active[0] == menu[0] check in _open
                if (event.type === "click" && menu.is(":visible") && this.active && this.active[0] === menu[0]) {
                    this._close();
                    return;
                }
                if (event.type === "mouseenter") {
                    this.element.find(":focus").focusout();
                    if (this.stashedOpenMenu) {
                        this._open(event, menu);
                    }
                    this.stashedOpenMenu = undefined;
                }
                if ((this.open && event.type === "mouseenter") || event.type === "click" || this.options.autoExpand) {
                    if (this.options.autoExpand) {
                        clearTimeout(this.closeTimer);
                    }
                    this._open(event, menu);
                }
            };

            menubar._on(input, {
                click: mouseBehaviorCallback,
                focus: mouseBehaviorCallback,
                mouseenter: mouseBehaviorCallback
            });
        },
        __applyKeyboardBehaviorForSubmenuHavingMenuItem: function(input, menubar) {
            var keyboardBehaviorCallback = function(event) {
                switch (event.keyCode) {
                    case $.ui.keyCode.SPACE:
                    case $.ui.keyCode.UP:
                    case $.ui.keyCode.DOWN:
                        menubar._open(event, $(event.target).next());
                        event.preventDefault();
                        break;
                    case $.ui.keyCode.LEFT:
                        this.previous(event);
                        event.preventDefault();
                        break;
                    case $.ui.keyCode.RIGHT:
                        this.next(event);
                        event.preventDefault();
                        break;
                }
            };

            menubar._on(input, {
                keydown: keyboardBehaviorCallback
            });
        },
        __applyMouseBehaviorForSubmenulessMenuItem: function($anItem, menubar) {
            menubar._off($anItem, "click mouseenter");
            menubar._hoverable($anItem);
            menubar._on($anItem, {
                click: function(event) {
                    if (this.active) {
                        this._close();
                    } else {
                        this.open = true;
                        this.active = $($anItem).parent();
                    }
                },
                mouseenter: function(event) {
                    if (this.open) {
                        this.stashedOpenMenu = this.active;
                        this._close();
                    }
                }
            });
        },
        __applyKeyboardBehaviorForSubmenulessMenuItem: function($anItem, menubar) {
            var behavior = function(event) {
                if (event.keyCode === $.ui.keyCode.LEFT) {
                    this.previous(event);
                    event.preventDefault();
                } else if (event.keyCode === $.ui.keyCode.RIGHT) {
                    this.next(event);
                    event.preventDefault();
                }
            };
            menubar._on($anItem, {
                keydown: behavior
            });
        },
        _destroy: function() {
            this.menuItems
                    .removeClass("ui-menubar-item")
                    .removeAttr("role");

            this.element
                    .removeClass("ui-menubar ui-widget-header ui-helper-clearfix")
                    .removeAttr("role")
                    .unbind(".menubar");

            this.items
                    .unbind(".menubar")
                    .removeClass("ui-button ui-widget ui-button-text-only ui-menubar-link ui-state-default")
                    .removeAttr("role")
                    .removeAttr("aria-haspopup")
                    // TODO unwrap?
                    .children("span.ui-button-text").each(function(i, e) {
                var item = $(this);
                item.parent().html(item.html());
            })
                    .end()
                    .children(".ui-icon").remove();

            this.element.find(":ui-menu")
                    .menu("destroy")
                    .show()
                    .removeAttr("aria-hidden")
                    .removeAttr("aria-expanded")
                    .removeAttr("tabindex")
                    .unbind(".menubar");
        },
        _close: function() {
            if (!this.active || !this.active.length) {
                return;
            }

            if (this.active.closest(this.options.items).data("hasSubMenu")) {
                this.active
                        .menu("collapseAll")
                        .hide()
                        .attr({
                    "aria-hidden": "true",
                    "aria-expanded": "false"
                });
                this.active
                        .prev()
                        .removeClass("ui-state-active");
                this.active.closest(this.options.items).removeClass("ui-state-active");
            } else {
                this.active
                        .attr({
                    "aria-hidden": "true",
                    "aria-expanded": "false"
                });
            }

            this.active = null;
            this.open = false;
            this.openSubmenus = 0;
        },
        _open: function(event, menu) {
            var button,
                    menuItem = menu.closest(".ui-menubar-item");

            if (this.active && this.active.length) {
                // TODO refactor, almost the same as _close above, but don't remove tabIndex
                if (this.active.closest(this.options.items).data("hasSubMenu")) {
                    this.active
                            .menu("collapseAll")
                            .hide()
                            .attr({
                        "aria-hidden": "true",
                        "aria-expanded": "false"
                    });
                    this.active.closest(this.options.items)
                            .removeClass("ui-state-active");
                } else {
                    this.active.removeClass("ui-state-active");
                }
            }

            // set tabIndex -1 to have the button skipped on shift-tab when menu is open (it gets focus)
            button = menuItem.addClass("ui-state-active").attr("tabIndex", -1);

            this.active = menu
                    .show()
                    .position($.extend({
                of: button
            }, this.options.position))
                    .removeAttr("aria-hidden")
                    .attr("aria-expanded", "true")
                    .menu("focus", event, menu.children(".ui-menu-item").first())
                    // TODO need a comment here why both events are triggered
                    .focus()
                    .focusin();

            this.open = true;
        },
        next: function(event) {
            if (this.open && this.active &&
                    this.active.closest(this.options.items).data("hasSubMenu") &&
                    this.active.data("menu").active &&
                    this.active.data("menu").active.has(".ui-menu").length) {
                // Track number of open submenus and prevent moving to next menubar item
                this.openSubmenus++;
                return;
            }
            this.openSubmenus = 0;
            this._move("next", "first", event);

        },
        previous: function(event) {
            if (this.open && this.openSubmenus) {
                // Track number of open submenus and prevent moving to previous menubar item
                this.openSubmenus--;
                return;
            }
            this.openSubmenus = 0;
            this._move("prev", "last", event);
        },
        _move: function(direction, filter, event) {
            var next,
                    wrapItem;

            var closestMenuItem = $(event.target).closest(".ui-menubar-item"),
                    nextMenuItem = closestMenuItem.data(direction + "MenuItem"),
                    focusableTarget = nextMenuItem.find("a, button");

            if (this.open) {
                if (nextMenuItem.data("hasSubMenu")) {
                    this._open(event, nextMenuItem.children(".ui-menu"));
                } else {
                    this._submenuless_open(event, nextMenuItem);
                }
            }

            focusableTarget.focus();
        },
        _submenuless_open: function(event, next) {
            var button,
                    menuItem = next.closest(".ui-menubar-item");

            if (this.active && this.active.length) {
                // TODO refactor, almost the same as _close above, but don't remove tabIndex
                if (this.active.closest(this.options.items)) {
                    this.active
                            .menu("collapseAll")
                            .hide()
                            .attr({
                        "aria-hidden": "true",
                        "aria-expanded": "false"
                    });
                }
                this.active.closest(this.options.items)
                        .removeClass("ui-state-active");
            }

            // set tabIndex -1 to have the button skipped on shift-tab when menu is open (it gets focus)
            button = menuItem.attr("tabIndex", -1);

            this.open = true;
            this.active = menuItem;
        }

    });


    function setSlider($scrollpane) {//$scrollpane is the div to be scrolled

        //set options for handle image - amend this to true or false as required
        var handleImage = false;

        //change the main div to overflow-hidden as we can use the slider now
        $scrollpane.css('overflow', 'hidden');

        //if it's not already there, wrap an extra div around the scrollpane so we can use the mousewheel later
        if ($scrollpane.parent('.scroll-container').length == 0)
            $scrollpane.wrap('<\div class="scroll-container"> /');
        //and again, if it's not there, wrap a div around the contents of the scrollpane to allow the scrolling
        if ($scrollpane.find('.scroll-content').length == 0)
            $scrollpane.children().wrapAll('<\div class="scroll-content"> /');

        //compare the height of the scroll content to the scroll pane to see if we need a scrollbar
        var difference = $scrollpane.find('.scroll-content').height() - $scrollpane.height();//eg it's 200px longer 
        $scrollpane.data('difference', difference);

        if (difference <= 0 && $scrollpane.find('.slider-wrap').length > 0)//scrollbar exists but is no longer required
        {
            $scrollpane.find('.slider-wrap').remove();//remove the scrollbar
            $scrollpane.find('.scroll-content').css({top: 0});//and reset the top position
        }

        if (difference > 0)//if the scrollbar is needed, set it up...
        {
            var proportion = difference / $scrollpane.find('.scroll-content').height();//eg 200px/500px

            var handleHeight = Math.round((1 - proportion) * $scrollpane.height());//set the proportional height - round it to make sure everything adds up correctly later on
            handleHeight -= handleHeight % 2;

            //if the slider has already been set up and this function is called again, we may need to set the position of the slider handle
            var contentposition = $scrollpane.find('.scroll-content').position();
            var sliderInitial = 100 * (1 - Math.abs(contentposition.top) / difference);

            if ($scrollpane.find('.slider-wrap').length == 0)//if the slider-wrap doesn't exist, insert it and set the initial value
            {
                $scrollpane.append('<\div class="slider-wrap"><\div class="slider-vertical"><\/div><\/div>');//append the necessary divs so they're only there if needed
                sliderInitial = 100;
            }

            $scrollpane.find('.slider-wrap').height($scrollpane.height());//set the height of the slider bar to that of the scroll pane

            //set up the slider 
            $scrollpane.find('.slider-vertical').slider({
                orientation: 'vertical',
                min: 0,
                max: 100,
                range: 'min',
                value: sliderInitial,
                slide: function(event, ui) {
                    var topValue = -((100 - ui.value) * difference / 100);
                    $scrollpane.find('.scroll-content').css({top: topValue});//move the top up (negative value) by the percentage the slider has been moved times the difference in height
                    $('ui-slider-range').height(ui.value + '%');//set the height of the range element
                },
                change: function(event, ui) {
                    var topValue = -((100 - ui.value) * ($scrollpane.find('.scroll-content').height() - $scrollpane.height()) / 100);//recalculate the difference on change
                    $scrollpane.find('.scroll-content').css({top: topValue});//move the top up (negative value) by the percentage the slider has been moved times the difference in height
                    $('ui-slider-range').height(ui.value + '%');
                }
            });

            //set the handle height and bottom margin so the middle of the handle is in line with the slider
            $scrollpane.find(".ui-slider-handle").css({height: handleHeight, 'margin-bottom': -0.5 * handleHeight});
            var origSliderHeight = $scrollpane.height();//read the original slider height
            var sliderHeight = origSliderHeight - handleHeight;//the height through which the handle can move needs to be the original height minus the handle height
            var sliderMargin = (origSliderHeight - sliderHeight) * 0.5;//so the slider needs to have both top and bottom margins equal to half the difference
            $scrollpane.find(".ui-slider").css({height: sliderHeight, 'margin-top': sliderMargin});//set the slider height and margins
            $scrollpane.find(".ui-slider-range").css({bottom: -sliderMargin});//position the slider-range div at the top of the slider container

            //if required create elements to hold the images for the scrollbar handle
            if (handleImage) {
                $(".ui-slider-handle").append('<img class="scrollbar-top" src="/images/misc/scrollbar-handle-top.png"/>');
                $(".ui-slider-handle").append('<img class="scrollbar-bottom" src="/images/misc/scrollbar-handle-bottom.png"/>');
                $(".ui-slider-handle").append('<img class="scrollbar-grip" src="/images/misc/scrollbar-handle-grip.png"/>');
            }
        }//end if

        //code for clicks on the scrollbar outside the slider
        $(".ui-slider").click(function(event) {//stop any clicks on the slider propagating through to the code below
            event.stopPropagation();
        });

        $(".slider-wrap").click(function(event) {//clicks on the wrap outside the slider range
            var offsetTop = $(this).offset().top;//read the offset of the scroll pane
            var clickValue = (event.pageY - offsetTop) * 100 / $(this).height();//find the click point, subtract the offset, and calculate percentage of the slider clicked
            $(this).find(".slider-vertical").slider("value", 100 - clickValue);//set the new value of the slider
        });


        //additional code for mousewheel
        if ($.fn.mousewheel) {

            $scrollpane.parent().unmousewheel();//remove any previously attached mousewheel events
            $scrollpane.parent().mousewheel(function(event, delta) {

                var speed = Math.round(5000 / $scrollpane.data('difference'));
                if (speed < 1)
                    speed = 1;
                if (speed > 100)
                    speed = 100;

                var sliderVal = $(this).find(".slider-vertical").slider("value");//read current value of the slider

                sliderVal += (delta * speed);//increment the current value

                $(this).find(".slider-vertical").slider("value", sliderVal);//and set the new value of the slider

                event.preventDefault();//stop any default behaviour
            });

        }

    }
}(jQuery));

/*
 * jQuery MiniColors: A tiny color picker built on jQuery
 *
 * Copyright Cory LaViska for A Beautiful Site, LLC. (http://www.abeautifulsite.net/)
 *
 * Dual-licensed under the MIT and GPL Version 2 licenses
 *
*/
if(jQuery) (function($) {
	
	// Yay, MiniColors!
	$.minicolors = {
		// Default settings
		defaultSettings: {
			animationSpeed: 100,
			animationEasing: 'swing',
			change: null,
			changeDelay: 0,
			control: 'hue',
			defaultValue: '',
			hide: null,
			hideSpeed: 100,
			inline: false,
			letterCase: 'lowercase',
			opacity: false,
			position: 'default',
			show: null,
			showSpeed: 100,
			swatchPosition: 'left',
			textfield: true,
			theme: 'default'
		}
	};
	
	// Public methods
	$.extend($.fn, {
		minicolors: function(method, data) {
			
			switch(method) {
				
				// Destroy the control
				case 'destroy':
					$(this).each( function() {
						destroy($(this));
					});
					return $(this);
				
				// Hide the color picker
				case 'hide':
					hide();
					return $(this);
				
				// Get/set opacity
				case 'opacity':
					if( data === undefined ) {
						// Getter
						return $(this).attr('data-opacity');
					} else {
						// Setter
						$(this).each( function() {
							refresh($(this).attr('data-opacity', data));
						});
						return $(this);
					}
				
				// Get an RGB(A) object based on the current color/opacity
				case 'rgbObject':
					return rgbObject($(this), method === 'rgbaObject');
				
				// Get an RGB(A) string based on the current color/opacity
				case 'rgbString':
				case 'rgbaString':
					return rgbString($(this), method === 'rgbaString')
				
				// Get/set settings on the fly
				case 'settings':
					if( data === undefined ) {
						return $(this).data('minicolors-settings');
					} else {
						// Setter
						$(this).each( function() {
							var settings = $(this).data('minicolors-settings') || {};
							destroy($(this));
							$(this).minicolors($.extend(true, settings, data));
						});
						return $(this);
					}
				
				// Show the color picker
				case 'show':
					show( $(this).eq(0) );
					return $(this);
				
				// Get/set the hex color value
				case 'value':
					if( data === undefined ) {
						// Getter
						return $(this).val();
					} else {
						// Setter
						$(this).each( function() {
							refresh($(this).val(data));
						});
						return $(this);
					}
				
				// Initializes the control
				case 'create':
				default:
					if( method !== 'create' ) data = method;
					$(this).each( function() {
						init($(this), data);
					});
					return $(this);
				
			}
			
		}
	});
	
	// Initialize input elements
	function init(input, settings) {
		
		var minicolors = $('<span class="minicolors" />'),
			defaultSettings = $.minicolors.defaultSettings;
		
		// Do nothing if already initialized
		if( input.data('minicolors-initialized') ) return;
		
		// Handle settings
		settings = $.extend(true, {}, defaultSettings, settings);
		
		// The wrapper
		minicolors
			.addClass('minicolors-theme-' + settings.theme)
			.addClass('minicolors-swatch-position-' + settings.swatchPosition)
			.toggleClass('minicolors-swatch-left', settings.swatchPosition === 'left')
			.toggleClass('minicolors-with-opacity', settings.opacity);
		
		// Custom positioning
		if( settings.position !== undefined ) {
			$.each(settings.position.split(' '), function() {
				minicolors.addClass('minicolors-position-' + this);
			});
		}
		
		// The input
		input
			.addClass('minicolors-input')
			.data('minicolors-initialized', true)
			.data('minicolors-settings', settings)
			.prop('size', 7)
			.prop('maxlength', 7)
			.wrap(minicolors)
			.after(
				'<span class="minicolors-panel minicolors-slider-' + settings.control + '">' + 
					'<span class="minicolors-slider">' + 
						'<span class="minicolors-picker"></span>' +
					'</span>' + 
					'<span class="minicolors-opacity-slider">' + 
						'<span class="minicolors-picker"></span>' +
					'</span>' +
					'<span class="minicolors-grid">' +
						'<span class="minicolors-grid-inner"></span>' +
						'<span class="minicolors-picker"><span></span></span>' +
					'</span>' +
				'</span>'
			);
		
		// Prevent text selection in IE
		input.parent().find('.minicolors-panel').on('selectstart', function() { return false; }).end();
		
		// Detect swatch position
		if( settings.swatchPosition === 'left' ) {
			// Left
			input.before('<span class="minicolors-swatch"><span></span></span>');
		} else {
			// Right
			input.after('<span class="minicolors-swatch"><span></span></span>');
		}
		
		// Disable textfield
		if( !settings.textfield ) input.addClass('minicolors-hidden');
		
		// Inline controls
		if( settings.inline ) input.parent().addClass('minicolors-inline');
		
		updateFromInput(input, false, true);
		
	}
	
	// Returns the input back to its original state
	function destroy(input) {
		
		var minicolors = input.parent();
		
		// Revert the input element
		input
			.removeData('minicolors-initialized')
			.removeData('minicolors-settings')
			.removeProp('size')
			.prop('maxlength', null)
			.removeClass('minicolors-input');
		
		// Remove the wrap and destroy whatever remains
		minicolors.before(input).remove();
		
	}
	
	// Refresh the specified control
	function refresh(input) {
		updateFromInput(input);
	}
	
	// Shows the specified dropdown panel
	function show(input) {
		
		var minicolors = input.parent(),
			panel = minicolors.find('.minicolors-panel'),
			settings = input.data('minicolors-settings');
		
		// Do nothing if uninitialized, disabled, inline, or already open
		if( !input.data('minicolors-initialized') || 
			input.prop('disabled') || 
			minicolors.hasClass('minicolors-inline') || 
			minicolors.hasClass('minicolors-focus')
		) return;
		
		hide();
		
		minicolors.addClass('minicolors-focus');
		panel
			.stop(true, true)
			.fadeIn(settings.showSpeed, function() {
				if( settings.show ) settings.show.call(input.get(0));
			});
		
	}
	
	// Hides all dropdown panels
	function hide() {
		
		$('.minicolors-input').each( function() {
			
			var input = $(this),
				settings = input.data('minicolors-settings'),
				minicolors = input.parent();
			
			// Don't hide inline controls
			if( settings.inline ) return;
			
			minicolors.find('.minicolors-panel').fadeOut(settings.hideSpeed, function() {
				if(minicolors.hasClass('minicolors-focus')) {
					if( settings.hide ) settings.hide.call(input.get(0));
				}
				minicolors.removeClass('minicolors-focus');
			});			
						
		});
	}
	
	// Moves the selected picker
	function move(target, event, animate) {
		
		var input = target.parents('.minicolors').find('.minicolors-input'),
			settings = input.data('minicolors-settings'),
			picker = target.find('[class$=-picker]'),
			offsetX = target.offset().left,
			offsetY = target.offset().top,
			x = Math.round(event.pageX - offsetX),
			y = Math.round(event.pageY - offsetY),
			duration = animate ? settings.animationSpeed : 0,
			wx, wy, r, phi;
			
		
		// Touch support
		if( event.originalEvent.changedTouches ) {
			x = event.originalEvent.changedTouches[0].pageX - offsetX;
			y = event.originalEvent.changedTouches[0].pageY - offsetY;
		}
		
		// Constrain picker to its container
		if( x < 0 ) x = 0;
		if( y < 0 ) y = 0;
		if( x > target.width() ) x = target.width();
		if( y > target.height() ) y = target.height();
		
		// Constrain color wheel values to the wheel
		if( target.parent().is('.minicolors-slider-wheel') && picker.parent().is('.minicolors-grid') ) {
			wx = 75 - x;
			wy = 75 - y;
			r = Math.sqrt(wx * wx + wy * wy);
			phi = Math.atan2(wy, wx);
			if( phi < 0 ) phi += Math.PI * 2;
			if( r > 75 ) {
				r = 75;
				x = 75 - (75 * Math.cos(phi));
				y = 75 - (75 * Math.sin(phi));
			}
			x = Math.round(x);
			y = Math.round(y);
		}
		
		// Move the picker
		if( target.is('.minicolors-grid') ) {
			picker
				.stop(true)
				.animate({
					top: y + 'px',
					left: x + 'px'
				}, duration, settings.animationEasing, function() {
					updateFromControl(input, target);
				});
		} else {
			picker
				.stop(true)
				.animate({
					top: y + 'px'
				}, duration, settings.animationEasing, function() {
					updateFromControl(input, target);
				});
		}
		
	}
	
	// Sets the input based on the color picker values
	function updateFromControl(input, target) {
		
		function getCoords(picker, container) {
			
			var left, top;
			if( !picker.length || !container ) return null;
			left = picker.offset().left;
			top = picker.offset().top;
			
			return {
				x: left - container.offset().left + (picker.outerWidth() / 2),
				y: top - container.offset().top + (picker.outerHeight() / 2)
			};
			
		}
		
		var hue, saturation, brightness, rgb, x, y, r, phi,
			
			hex = input.val(),
			opacity = input.attr('data-opacity'),
			
			// Helpful references
			minicolors = input.parent(),
			settings = input.data('minicolors-settings'),
			panel = minicolors.find('.minicolors-panel'),
			swatch = minicolors.find('.minicolors-swatch'),
			
			// Panel objects
			grid = minicolors.find('.minicolors-grid'),
			slider = minicolors.find('.minicolors-slider'),
			opacitySlider = minicolors.find('.minicolors-opacity-slider'),
			
			// Picker objects
			gridPicker = grid.find('[class$=-picker]'),
			sliderPicker = slider.find('[class$=-picker]'),
			opacityPicker = opacitySlider.find('[class$=-picker]'),
			
			// Picker positions
			gridPos = getCoords(gridPicker, grid),
			sliderPos = getCoords(sliderPicker, slider),
			opacityPos = getCoords(opacityPicker, opacitySlider);
		
		// Handle colors
		if( target.is('.minicolors-grid, .minicolors-slider') ) {
			
			// Determine HSB values
			switch(settings.control) {
				
				case 'wheel':
					// Calculate hue, saturation, and brightness
					x = (grid.width() / 2) - gridPos.x;
					y = (grid.height() / 2) - gridPos.y;
					r = Math.sqrt(x * x + y * y);
					phi = Math.atan2(y, x);
					if( phi < 0 ) phi += Math.PI * 2;
					if( r > 75 ) {
						r = 75;
						gridPos.x = 69 - (75 * Math.cos(phi));
						gridPos.y = 69 - (75 * Math.sin(phi));
					}
					saturation = keepWithin(r / 0.75, 0, 100);
					hue = keepWithin(phi * 180 / Math.PI, 0, 360);
					brightness = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
					hex = hsb2hex({
						h: hue,
						s: saturation,
						b: brightness
					});
					
					// Update UI
					slider.css('backgroundColor', hsb2hex({ h: hue, s: saturation, b: 100 }));
					break;
				
				case 'saturation':
					// Calculate hue, saturation, and brightness
					hue = keepWithin(parseInt(gridPos.x * (360 / grid.width())), 0, 360);
					saturation = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
					brightness = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
					hex = hsb2hex({
						h: hue,
						s: saturation,
						b: brightness
					});
					
					// Update UI
					slider.css('backgroundColor', hsb2hex({ h: hue, s: 100, b: brightness }));
					minicolors.find('.minicolors-grid-inner').css('opacity', saturation / 100);
					break;
				
				case 'brightness':
					// Calculate hue, saturation, and brightness
					hue = keepWithin(parseInt(gridPos.x * (360 / grid.width())), 0, 360);
					saturation = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
					brightness = keepWithin(100 - Math.floor(sliderPos.y * (100 / slider.height())), 0, 100);
					hex = hsb2hex({
						h: hue,
						s: saturation,
						b: brightness
					});
					
					// Update UI
					slider.css('backgroundColor', hsb2hex({ h: hue, s: saturation, b: 100 }));
					minicolors.find('.minicolors-grid-inner').css('opacity', 1 - (brightness / 100));
					break;
				
				default:
					// Calculate hue, saturation, and brightness
					hue = keepWithin(360 - parseInt(sliderPos.y * (360 / slider.height())), 0, 360);
					saturation = keepWithin(Math.floor(gridPos.x * (100 / grid.width())), 0, 100);
					brightness = keepWithin(100 - Math.floor(gridPos.y * (100 / grid.height())), 0, 100);
					hex = hsb2hex({
						h: hue,
						s: saturation,
						b: brightness
					});
					
					// Update UI
					grid.css('backgroundColor', hsb2hex({ h: hue, s: 100, b: 100 }));
					break;
				
			}
		
			// Adjust case
	    	input.val( convertCase(hex, settings.letterCase) );
	    	
		}
		
		// Handle opacity
		if( target.is('.minicolors-opacity-slider') ) {
			if( settings.opacity ) {
				opacity = parseFloat(1 - (opacityPos.y / opacitySlider.height())).toFixed(2);
			} else {
				opacity = 1;
			}
			if( settings.opacity ) input.attr('data-opacity', opacity);
		}
		
		// Set swatch color
		swatch.find('SPAN').css({
			backgroundColor: hex,
			opacity: opacity
		});
		
		// Handle change event
		doChange(input, hex, opacity);
		
	}
	
	// Sets the color picker values from the input
	function updateFromInput(input, preserveInputValue, firstRun) {
		
		var hex,
			hsb,
			opacity,
			x, y, r, phi,
			
			// Helpful references
			minicolors = input.parent(),
			settings = input.data('minicolors-settings'),
			swatch = minicolors.find('.minicolors-swatch'),
			
			// Panel objects
			grid = minicolors.find('.minicolors-grid'),
			slider = minicolors.find('.minicolors-slider'),
			opacitySlider = minicolors.find('.minicolors-opacity-slider'),
			
			// Picker objects
			gridPicker = grid.find('[class$=-picker]'),
			sliderPicker = slider.find('[class$=-picker]'),
			opacityPicker = opacitySlider.find('[class$=-picker]');
		
		// Determine hex/HSB values
		hex = convertCase(parseHex(input.val(), true), settings.letterCase);
		if( !hex ) hex = convertCase(parseHex(settings.defaultValue, true));
		hsb = hex2hsb(hex);
		
		// Update input value
		if( !preserveInputValue ) input.val(hex);
		
		// Determine opacity value
		if( settings.opacity ) {
			// Get from data-opacity attribute and keep within 0-1 range
			opacity = input.attr('data-opacity') === '' ? 1 : keepWithin(parseFloat(input.attr('data-opacity')).toFixed(2), 0, 1);
			if( isNaN(opacity) ) opacity = 1;
			input.attr('data-opacity', opacity);
			swatch.find('SPAN').css('opacity', opacity);
			
			// Set opacity picker position
			y = keepWithin(opacitySlider.height() - (opacitySlider.height() * opacity), 0, opacitySlider.height());
			opacityPicker.css('top', y + 'px');
		}
		
		// Update swatch
		swatch.find('SPAN').css('backgroundColor', hex);
		
		// Determine picker locations
		switch(settings.control) {
			
			case 'wheel':
				// Set grid position
				r = keepWithin(Math.ceil(hsb.s * 0.75), 0, grid.height() / 2);
				phi = hsb.h * Math.PI / 180;
				x = keepWithin(75 - Math.cos(phi) * r, 0, grid.width());
				y = keepWithin(75 - Math.sin(phi) * r, 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});
				
				// Set slider position
				y = 150 - (hsb.b / (100 / grid.height()));
				if( hex === '' ) y = 0;
				sliderPicker.css('top', y + 'px');
				
				// Update panel color
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: hsb.s, b: 100 }));
				break;
			
			case 'saturation':
				// Set grid position
				x = keepWithin((5 * hsb.h) / 12, 0, 150);
				y = keepWithin(grid.height() - Math.ceil(hsb.b / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});				
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.s * (slider.height() / 100)), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: 100, b: hsb.b }));
				minicolors.find('.minicolors-grid-inner').css('opacity', hsb.s / 100);
				
				break;
			
			case 'brightness':
				// Set grid position
				x = keepWithin((5 * hsb.h) / 12, 0, 150);
				y = keepWithin(grid.height() - Math.ceil(hsb.s / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});				
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.b * (slider.height() / 100)), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update UI
				slider.css('backgroundColor', hsb2hex({ h: hsb.h, s: hsb.s, b: 100 }));
				minicolors.find('.minicolors-grid-inner').css('opacity', 1 - (hsb.b / 100));
				break;
			
			default:
				// Set grid position
				x = keepWithin(Math.ceil(hsb.s / (100 / grid.width())), 0, grid.width());
				y = keepWithin(grid.height() - Math.ceil(hsb.b / (100 / grid.height())), 0, grid.height());
				gridPicker.css({
					top: y + 'px',
					left: x + 'px'
				});
				
				// Set slider position
				y = keepWithin(slider.height() - (hsb.h / (360 / slider.height())), 0, slider.height());
				sliderPicker.css('top', y + 'px');
				
				// Update panel color
				grid.css('backgroundColor', hsb2hex({ h: hsb.h, s: 100, b: 100 }));
				break;
				
		}
		
		// Handle change event
		if( !firstRun ) doChange(input, hex, opacity);
		
	}
	
	// Runs the change and changeDelay callbacks
	function doChange(input, hex, opacity) {
		
		var settings = input.data('minicolors-settings');
		
		// Only run if it actually changed
		if( hex + opacity !== input.data('minicolors-lastChange') ) {
			
			// Remember last-changed value
			input.data('minicolors-lastChange', hex + opacity);
			
			// Fire change event
			if( settings.change ) {
				if( settings.changeDelay ) {
					// Call after a delay
					clearTimeout(input.data('minicolors-changeTimeout'));
					input.data('minicolors-changeTimeout', setTimeout( function() {
						settings.change.call(input.get(0), hex, opacity);
					}, settings.changeDelay));
				} else {
					// Call immediately
					settings.change.call(input.get(0), hex, opacity);
				}
			}
			
		}
	
	}
	
	// Generates an RGB(A) object based on the input's value
	function rgbObject(input) {
		var hex = parseHex($(input).val(), true),
			rgb = hex2rgb(hex),
			opacity = $(input).attr('data-opacity');
		if( !rgb ) return null;
		if( opacity !== undefined ) $.extend(rgb, { a: parseFloat(opacity) });
		return rgb;
	}
	
	// Genearates an RGB(A) string based on the input's value
	function rgbString(input, alpha) {
		var hex = parseHex($(input).val(), true),
			rgb = hex2rgb(hex),
			opacity = $(input).attr('data-opacity');
		if( !rgb ) return null;
		if( opacity === undefined ) opacity = 1;
		if( alpha ) {
			return 'rgba(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ', ' + parseFloat(opacity) + ')';
		} else {
			return 'rgb(' + rgb.r + ', ' + rgb.g + ', ' + rgb.b + ')';
		}
	}
	
	// Converts to the letter case specified in settings
	function convertCase(string, letterCase) {
		return letterCase === 'uppercase' ? string.toUpperCase() : string.toLowerCase();
	}
	
	// Parses a string and returns a valid hex string when possible
	function parseHex(string, expand) {
		string = string.replace(/[^A-F0-9]/ig, '');
		if( string.length !== 3 && string.length !== 6 ) return '';
		if( string.length === 3 && expand ) {
			string = string[0] + string[0] + string[1] + string[1] + string[2] + string[2];
		}
		return '#' + string;
	}
	
	// Keeps value within min and max
	function keepWithin(value, min, max) {
		if( value < min ) value = min;
		if( value > max ) value = max;
		return value;
	}
	
	// Converts an HSB object to an RGB object
	function hsb2rgb(hsb) {
		var rgb = {};
		var h = Math.round(hsb.h);
		var s = Math.round(hsb.s * 255 / 100);
		var v = Math.round(hsb.b * 255 / 100);
		if(s === 0) {
			rgb.r = rgb.g = rgb.b = v;
		} else {
			var t1 = v;
			var t2 = (255 - s) * v / 255;
			var t3 = (t1 - t2) * (h % 60) / 60;
			if( h === 360 ) h = 0;
			if( h < 60 ) { rgb.r = t1; rgb.b = t2; rgb.g = t2 + t3; }
			else if( h < 120 ) {rgb.g = t1; rgb.b = t2; rgb.r = t1 - t3; }
			else if( h < 180 ) {rgb.g = t1; rgb.r = t2; rgb.b = t2 + t3; }
			else if( h < 240 ) {rgb.b = t1; rgb.r = t2; rgb.g = t1 - t3; }
			else if( h < 300 ) {rgb.b = t1; rgb.g = t2; rgb.r = t2 + t3; }
			else if( h < 360 ) {rgb.r = t1; rgb.g = t2; rgb.b = t1 - t3; }
			else { rgb.r = 0; rgb.g = 0; rgb.b = 0; }
		}
		return {
			r: Math.round(rgb.r),
			g: Math.round(rgb.g),
			b: Math.round(rgb.b)
		};
	}
	
	// Converts an RGB object to a hex string
	function rgb2hex(rgb) {
		var hex = [
			rgb.r.toString(16),
			rgb.g.toString(16),
			rgb.b.toString(16)
		];
		$.each(hex, function(nr, val) {
			if (val.length === 1) hex[nr] = '0' + val;
		});
		return '#' + hex.join('');
	}
	
	// Converts an HSB object to a hex string
	function hsb2hex(hsb) {
		return rgb2hex(hsb2rgb(hsb));
	}
	
	// Converts a hex string to an HSB object
	function hex2hsb(hex) {
		var hsb = rgb2hsb(hex2rgb(hex));
		if( hsb.s === 0 ) hsb.h = 360;
		return hsb;
	}
	
	// Converts an RGB object to an HSB object
	function rgb2hsb(rgb) {
		var hsb = { h: 0, s: 0, b: 0 };
		var min = Math.min(rgb.r, rgb.g, rgb.b);
		var max = Math.max(rgb.r, rgb.g, rgb.b);
		var delta = max - min;
		hsb.b = max;
		hsb.s = max !== 0 ? 255 * delta / max : 0;
		if( hsb.s !== 0 ) {
			if( rgb.r === max ) {
				hsb.h = (rgb.g - rgb.b) / delta;
			} else if( rgb.g === max ) {
				hsb.h = 2 + (rgb.b - rgb.r) / delta;
			} else {
				hsb.h = 4 + (rgb.r - rgb.g) / delta;
			}
		} else {
			hsb.h = -1;
		}
		hsb.h *= 60;
		if( hsb.h < 0 ) {
			hsb.h += 360;
		}
		hsb.s *= 100/255;
		hsb.b *= 100/255;
		return hsb;
	}
	
	// Converts a hex string to an RGB object
	function hex2rgb(hex) {
		hex = parseInt(((hex.indexOf('#') > -1) ? hex.substring(1) : hex), 16);
		return {
			r: hex >> 16,
			g: (hex & 0x00FF00) >> 8,
			b: (hex & 0x0000FF)
		};
	}
	
	// Handle events
	$(document)
		// Hide on clicks outside of the control
		.on('mousedown.minicolors touchstart.minicolors', function(event) {
			if( !$(event.target).parents().add(event.target).hasClass('minicolors') ) {
				hide();
			}
		})
		// Start moving
		.on('mousedown.minicolors touchstart.minicolors', '.minicolors-grid, .minicolors-slider, .minicolors-opacity-slider', function(event) {
			var target = $(this);
			event.preventDefault();
			$(document).data('minicolors-target', target);
			move(target, event, true);
		})
		// Move pickers
		.on('mousemove.minicolors touchmove.minicolors', function(event) {
			var target = $(document).data('minicolors-target');
			if( target ) move(target, event);
		})
		// Stop moving
		.on('mouseup.minicolors touchend.minicolors', function() {
			$(this).removeData('minicolors-target');
		})
		// Toggle panel when swatch is clicked
		.on('mousedown.minicolors touchstart.minicolors', '.minicolors-swatch', function(event) {
			event.preventDefault();
			var input = $(this).parent().find('.minicolors-input'),
				minicolors = input.parent();
			if( minicolors.hasClass('minicolors-focus') ) {
				hide(input);
			} else {
				show(input);
			}
		})
		// Show on focus
		.on('focus.minicolors', '.minicolors-input', function(event) {
			var input = $(this);
			if( !input.data('minicolors-initialized') ) return;
			show(input);
		})
		// Fix hex on blur
		.on('blur.minicolors', '.minicolors-input', function(event) {
			var input = $(this),
				settings = input.data('minicolors-settings');
			if( !input.data('minicolors-initialized') ) return;
			
			// Parse Hex
			input.val(parseHex(input.val(), true));
			
			// Is it blank?
			if( input.val() === '' ) input.val(parseHex(settings.defaultValue, true));
			
			// Adjust case
			input.val( convertCase(input.val(), settings.letterCase) );
			
		})
		// Handle keypresses
		.on('keydown.minicolors', '.minicolors-input', function(event) {
			var input = $(this);
			if( !input.data('minicolors-initialized') ) return;
			switch(event.keyCode) {
				case 9: // tab
					hide();
					break;
				case 13: // enter
				case 27: // esc
					hide();
					input.blur();
					break;
			}
		})
		// Update on keyup
		.on('keyup.minicolors', '.minicolors-input', function(event) {
			var input = $(this);
			if( !input.data('minicolors-initialized') ) return;
			updateFromInput(input, true);
		})
		// Update on paste
		.on('paste.minicolors', '.minicolors-input', function(event) {
			var input = $(this);
			if( !input.data('minicolors-initialized') ) return;
			setTimeout( function() {
				updateFromInput(input, true);
			}, 1);
		});
	
})(jQuery);

/// Adds zeros
function zeroPadder(number, length) {
    var string = '' + number;
    while (string.length < length) {
        string = '0' + string;
    }
    return string;
}

//// Private added functionality extending javascript
if (typeof String.prototype.startsWith !== 'function') {
  String.prototype.startsWith = function (str){
    return this.indexOf(str) === 0;
  };
}

function xmlToString(xmlData) { // this functions waits jQuery XML 
    var xmlString = undefined;
    if (window.ActiveXObject){
        xmlString = xmlData[0].xml;
    }
    if (xmlString === undefined){
        var oSerializer = new XMLSerializer();
        xmlString = oSerializer.serializeToString(xmlData[0]);
    }
    return xmlString;
}