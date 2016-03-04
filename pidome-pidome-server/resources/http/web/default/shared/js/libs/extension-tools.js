/*! ========================================================================
 * Bootstrap Toggle: bootstrap-toggle.js v2.2.0
 * http://www.bootstraptoggle.com
 * ========================================================================
 * Copyright 2014 Min Hur, The New York Times Company
 * Licensed under MIT
 * ======================================================================== */
+function(a){"use strict";function b(b){return this.each(function(){var d=a(this),e=d.data("bs.toggle"),f="object"==typeof b&&b;e||d.data("bs.toggle",e=new c(this,f)),"string"==typeof b&&e[b]&&e[b]()})}var c=function(b,c){this.$element=a(b),this.options=a.extend({},this.defaults(),c),this.render()};c.VERSION="2.2.0",c.DEFAULTS={on:"On",off:"Off",onstyle:"primary",offstyle:"default",size:"normal",style:"",width:null,height:null},c.prototype.defaults=function(){return{on:this.$element.attr("data-on")||c.DEFAULTS.on,off:this.$element.attr("data-off")||c.DEFAULTS.off,onstyle:this.$element.attr("data-onstyle")||c.DEFAULTS.onstyle,offstyle:this.$element.attr("data-offstyle")||c.DEFAULTS.offstyle,size:this.$element.attr("data-size")||c.DEFAULTS.size,style:this.$element.attr("data-style")||c.DEFAULTS.style,width:this.$element.attr("data-width")||c.DEFAULTS.width,height:this.$element.attr("data-height")||c.DEFAULTS.height}},c.prototype.render=function(){this._onstyle="btn-"+this.options.onstyle,this._offstyle="btn-"+this.options.offstyle;var b="large"===this.options.size?"btn-lg":"small"===this.options.size?"btn-sm":"mini"===this.options.size?"btn-xs":"",c=a('<label class="btn">').html(this.options.on).addClass(this._onstyle+" "+b),d=a('<label class="btn">').html(this.options.off).addClass(this._offstyle+" "+b+" active"),e=a('<span class="toggle-handle btn btn-default">').addClass(b),f=a('<div class="toggle-group">').append(c,d,e),g=a('<div class="toggle btn" data-toggle="toggle">').addClass(this.$element.prop("checked")?this._onstyle:this._offstyle+" off").addClass(b).addClass(this.options.style);this.$element.wrap(g),a.extend(this,{$toggle:this.$element.parent(),$toggleOn:c,$toggleOff:d,$toggleGroup:f}),this.$toggle.append(f);var h=this.options.width||Math.max(c.outerWidth(),d.outerWidth())+e.outerWidth()/2,i=this.options.height||Math.max(c.outerHeight(),d.outerHeight());c.addClass("toggle-on"),d.addClass("toggle-off"),this.$toggle.css({width:h,height:i}),this.options.height&&(c.css("line-height",c.height()+"px"),d.css("line-height",d.height()+"px")),this.update(!0),this.trigger(!0)},c.prototype.toggle=function(){this.$element.prop("checked")?this.off():this.on()},c.prototype.on=function(a){return this.$element.prop("disabled")?!1:(this.$toggle.removeClass(this._offstyle+" off").addClass(this._onstyle),this.$element.prop("checked",!0),void(a||this.trigger()))},c.prototype.off=function(a){return this.$element.prop("disabled")?!1:(this.$toggle.removeClass(this._onstyle).addClass(this._offstyle+" off"),this.$element.prop("checked",!1),void(a||this.trigger()))},c.prototype.enable=function(){this.$toggle.removeAttr("disabled"),this.$element.prop("disabled",!1)},c.prototype.disable=function(){this.$toggle.attr("disabled","disabled"),this.$element.prop("disabled",!0)},c.prototype.update=function(a){this.$element.prop("disabled")?this.disable():this.enable(),this.$element.prop("checked")?this.on(a):this.off(a)},c.prototype.trigger=function(b){this.$element.off("change.bs.toggle"),b||this.$element.change(),this.$element.on("change.bs.toggle",a.proxy(function(){this.update()},this))},c.prototype.destroy=function(){this.$element.off("change.bs.toggle"),this.$toggleGroup.remove(),this.$element.removeData("bs.toggle"),this.$element.unwrap()};var d=a.fn.bootstrapToggle;a.fn.bootstrapToggle=b,a.fn.bootstrapToggle.Constructor=c,a.fn.toggle.noConflict=function(){return a.fn.bootstrapToggle=d,this},a(function(){a("input[type=checkbox][data-toggle^=toggle]").bootstrapToggle()}),a(document).on("click.bs.toggle","div[data-toggle^=toggle]",function(b){var c=a(this).find("input[type=checkbox]");c.bootstrapToggle("toggle"),b.preventDefault()})}(jQuery);

/* ===========================================================
 * Bootstrap: inputmask.js v3.1.0
 * http://jasny.github.io/bootstrap/javascript/#inputmask
 * 
 * Based on Masked Input plugin by Josh Bush (digitalbush.com)
 * ===========================================================
 * Copyright 2012-2014 Arnold Daniels
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================== */

+function ($) { "use strict";

  var isIphone = (window.orientation !== undefined)
  var isAndroid = navigator.userAgent.toLowerCase().indexOf("android") > -1
  var isIE = window.navigator.appName == 'Microsoft Internet Explorer'

  // INPUTMASK PUBLIC CLASS DEFINITION
  // =================================

  var Inputmask = function (element, options) {
    if (isAndroid) return // No support because caret positioning doesn't work on Android
    
    this.$element = $(element)
    this.options = $.extend({}, Inputmask.DEFAULTS, options)
    this.mask = String(this.options.mask)
    
    this.init()
    this.listen()
        
    this.checkVal() //Perform initial check for existing values
  }

  Inputmask.DEFAULTS = {
    mask: "",
    placeholder: "_",
    definitions: {
      '9': "[0-9]",
      'a': "[A-Za-z]",
      'w': "[A-Za-z0-9]",
      '*': "."
    }
  }

  Inputmask.prototype.init = function() {
    var defs = this.options.definitions
    var len = this.mask.length

    this.tests = [] 
    this.partialPosition = this.mask.length
    this.firstNonMaskPos = null

    $.each(this.mask.split(""), $.proxy(function(i, c) {
      if (c == '?') {
        len--
        this.partialPosition = i
      } else if (defs[c]) {
        this.tests.push(new RegExp(defs[c]))
        if (this.firstNonMaskPos === null)
          this.firstNonMaskPos =  this.tests.length - 1
      } else {
        this.tests.push(null)
      }
    }, this))

    this.buffer = $.map(this.mask.split(""), $.proxy(function(c, i) {
      if (c != '?') return defs[c] ? this.options.placeholder : c
    }, this))

    this.focusText = this.$element.val()

    this.$element.data("rawMaskFn", $.proxy(function() {
      return $.map(this.buffer, function(c, i) {
        return this.tests[i] && c != this.options.placeholder ? c : null
      }).join('')
    }, this))
  }
    
  Inputmask.prototype.listen = function() {
    if (this.$element.attr("readonly")) return

    var pasteEventName = (isIE ? 'paste' : 'input') + ".bs.inputmask"

    this.$element
      .on("unmask.bs.inputmask", $.proxy(this.unmask, this))

      .on("focus.bs.inputmask", $.proxy(this.focusEvent, this))
      .on("blur.bs.inputmask", $.proxy(this.blurEvent, this))

      .on("keydown.bs.inputmask", $.proxy(this.keydownEvent, this))
      .on("keypress.bs.inputmask", $.proxy(this.keypressEvent, this))

      .on(pasteEventName, $.proxy(this.pasteEvent, this))
  }

  //Helper Function for Caret positioning
  Inputmask.prototype.caret = function(begin, end) {
    if (this.$element.length === 0) return
    if (typeof begin == 'number') {
      end = (typeof end == 'number') ? end : begin
      return this.$element.each(function() {
        if (this.setSelectionRange) {
          this.setSelectionRange(begin, end)
        } else if (this.createTextRange) {
          var range = this.createTextRange()
          range.collapse(true)
          range.moveEnd('character', end)
          range.moveStart('character', begin)
          range.select()
        }
      })
    } else {
      if (this.$element[0].setSelectionRange) {
        begin = this.$element[0].selectionStart
        end = this.$element[0].selectionEnd
      } else if (document.selection && document.selection.createRange) {
        var range = document.selection.createRange()
        begin = 0 - range.duplicate().moveStart('character', -100000)
        end = begin + range.text.length
      }
      return {
        begin: begin, 
        end: end
      }
    }
  }
  
  Inputmask.prototype.seekNext = function(pos) {
    var len = this.mask.length
    while (++pos <= len && !this.tests[pos]);

    return pos
  }
  
  Inputmask.prototype.seekPrev = function(pos) {
    while (--pos >= 0 && !this.tests[pos]);

    return pos
  }

  Inputmask.prototype.shiftL = function(begin,end) {
    var len = this.mask.length

    if (begin < 0) return

    for (var i = begin, j = this.seekNext(end); i < len; i++) {
      if (this.tests[i]) {
        if (j < len && this.tests[i].test(this.buffer[j])) {
          this.buffer[i] = this.buffer[j]
          this.buffer[j] = this.options.placeholder
        } else
          break
        j = this.seekNext(j)
      }
    }
    this.writeBuffer()
    this.caret(Math.max(this.firstNonMaskPos, begin))
  }

  Inputmask.prototype.shiftR = function(pos) {
    var len = this.mask.length

    for (var i = pos, c = this.options.placeholder; i < len; i++) {
      if (this.tests[i]) {
        var j = this.seekNext(i)
        var t = this.buffer[i]
        this.buffer[i] = c
        if (j < len && this.tests[j].test(t))
          c = t
        else
          break
      }
    }
  },

  Inputmask.prototype.unmask = function() {
    this.$element
      .unbind(".bs.inputmask")
      .removeData("bs.inputmask")
  }

  Inputmask.prototype.focusEvent = function() {
    this.focusText = this.$element.val()
    var len = this.mask.length 
    var pos = this.checkVal()
    this.writeBuffer()

    var that = this
    var moveCaret = function() {
      if (pos == len)
        that.caret(0, pos)
      else
        that.caret(pos)
    }

    moveCaret()
    setTimeout(moveCaret, 50)
  }

  Inputmask.prototype.blurEvent = function() {
    this.checkVal()
    if (this.$element.val() !== this.focusText) {
      this.$element.trigger('change')
      this.$element.trigger('input')
    }
  }

  Inputmask.prototype.keydownEvent = function(e) {
    var k = e.which

    //backspace, delete, and escape get special treatment
    if (k == 8 || k == 46 || (isIphone && k == 127)) {
      var pos = this.caret(),
      begin = pos.begin,
      end = pos.end

      if (end - begin === 0) {
        begin = k != 46 ? this.seekPrev(begin) : (end = this.seekNext(begin - 1))
        end = k == 46 ? this.seekNext(end) : end
      }
      this.clearBuffer(begin, end)
      this.shiftL(begin, end - 1)

      return false
    } else if (k == 27) {//escape
      this.$element.val(this.focusText)
      this.caret(0, this.checkVal())
      return false
    }
  }

  Inputmask.prototype.keypressEvent = function(e) {
    var len = this.mask.length

    var k = e.which,
    pos = this.caret()

    if (e.ctrlKey || e.altKey || e.metaKey || k < 32)  {//Ignore
      return true
    } else if (k) {
      if (pos.end - pos.begin !== 0) {
        this.clearBuffer(pos.begin, pos.end)
        this.shiftL(pos.begin, pos.end - 1)
      }

      var p = this.seekNext(pos.begin - 1)
      if (p < len) {
        var c = String.fromCharCode(k)
        if (this.tests[p].test(c)) {
          this.shiftR(p)
          this.buffer[p] = c
          this.writeBuffer()
          var next = this.seekNext(p)
          this.caret(next)
        }
      }
      return false
    }
  }

  Inputmask.prototype.pasteEvent = function() {
    var that = this

    setTimeout(function() {
      that.caret(that.checkVal(true))
    }, 0)
  }

  Inputmask.prototype.clearBuffer = function(start, end) {
    var len = this.mask.length

    for (var i = start; i < end && i < len; i++) {
      if (this.tests[i])
        this.buffer[i] = this.options.placeholder
    }
  }

  Inputmask.prototype.writeBuffer = function() {
    return this.$element.val(this.buffer.join('')).val()
  }

  Inputmask.prototype.checkVal = function(allow) {
    var len = this.mask.length
    //try to place characters where they belong
    var test = this.$element.val()
    var lastMatch = -1

    for (var i = 0, pos = 0; i < len; i++) {
      if (this.tests[i]) {
        this.buffer[i] = this.options.placeholder
        while (pos++ < test.length) {
          var c = test.charAt(pos - 1)
          if (this.tests[i].test(c)) {
            this.buffer[i] = c
            lastMatch = i
            break
          }
        }
        if (pos > test.length)
          break
      } else if (this.buffer[i] == test.charAt(pos) && i != this.partialPosition) {
        pos++
        lastMatch = i
      }
    }
    if (!allow && lastMatch + 1 < this.partialPosition) {
      this.$element.val("")
      this.clearBuffer(0, len)
    } else if (allow || lastMatch + 1 >= this.partialPosition) {
      this.writeBuffer()
      if (!allow) this.$element.val(this.$element.val().substring(0, lastMatch + 1))
    }
    return (this.partialPosition ? i : this.firstNonMaskPos)
  }

  
  // INPUTMASK PLUGIN DEFINITION
  // ===========================

  var old = $.fn.inputmask
  
  $.fn.inputmask = function (options) {
    return this.each(function () {
      var $this = $(this)
      var data = $this.data('bs.inputmask')
      
      if (!data) $this.data('bs.inputmask', (data = new Inputmask(this, options)))
    })
  }

  $.fn.inputmask.Constructor = Inputmask


  // INPUTMASK NO CONFLICT
  // ====================

  $.fn.inputmask.noConflict = function () {
    $.fn.inputmask = old
    return this
  }


  // INPUTMASK DATA-API
  // ==================

  $(document).on('focus.bs.inputmask.data-api', '[data-mask]', function (e) {
    var $this = $(this)
    if ($this.data('bs.inputmask')) return
    $this.inputmask($this.data())
  })

}(window.jQuery);

//// Forms checker
(function ( $ ) {

    $.fn.validate = function( options ) {

        var settings = $.extend({
            init: function() {},         /* init even */
            success: function() {},      /* sucess even */
            fail: function(invalids) {}  /* Fail even */
            /* join: ' ' */ /* Join array field data with it - If it be empty it will still remain array */
        }, options );


        return this.on('submit', function(e) {
            var form = this;
            var invalids = [];

            settings.init.call(form);

            var data = {};
            $('input,textarea,select', this).each(function() {
                var i = $(this);
                var name = i.attr('name');
                if(typeof(name) != 'undefined' && (i.is(":not([type='checkbox'],[type='radio'])") || i.is(":checked"))) {
                    var value = i.val();

                    if(typeof(data[name]) != 'undefined') {
                        data[name] = [].concat(data[name]).concat(value);
                    } else {
                        data[name] = value;
                    }
                }
            });

            /* join array if it's required */
            if (typeof(settings.join) != 'undefined') {
                for(var i in data) {
                    if (typeof(data[i]) == "function") {
                        data[i] = data[i].join(settings.join);
                    }
                }
            }

            /** [data-require] is deprecated use requried instead **/
            $("[data-regex],[data-require],[data-required],[required],[data-equals]", form).each(function() {
                var self = $(this);

                var regex = self.attr('data-regex');
                var required = self.is('[required]') || self.is('[data-required]') /* the rest deprecated */ || self.is('[data-require]');
                var equals = self.attr('data-equals');
                var value = self.val();

                if(self.is("[type='checkbox']") && !self.is(":checked")) value='';

                // replace value with total value
                var name = self.attr('name');
                if(typeof(name) != 'undefined') {
                    value = data[name];
                }

                if(typeof(equals) != 'undefined' && typeof(data[equals]) != 'undefined') {
                    if(value != data[equals]) {
                        invalids.push(this);
                        invalids.push($("[name='" + equals + "']")[0]);
                    }
                }

                /** array is always valid, because for array inputs we just check required **/
                if(!Array.isArray(value)) {
                    if(value && value.length > 0) {
                        if (typeof regex === 'undefined') {
                            var type = self.attr('data-type');
                            if(type && ($.inArray(self.attr('type').toLowerCase(), ['checkbox', 'radio'])==-1)) {
                                regex = type.toLowerCase();
                            }
                        }
                        if (regex) {
                            var r = (function(regex) {
                                switch(regex) {
                                    case 'name+family':
                                        return /^((?![0-9]).+\s.+)/g;
                                    case 'name':
                                    case 'family':
                                        return /^((?![0-9]).+)/g;
                                    case 'url':
                                        return /^(ht|f)tps?:\/\/[a-z0-9-\.]+\.[a-z]{2,4}\/?([^\s<>\#%"\,\{\}\\|\\\^\[\]`]+)?$/;
                                    case 'date':
                                        return /^(0?[1-9]|[12][0-9]|3[01])[\/\-](0?[1-9]|1[012])[\/\-]\d{4}$/;
                                    case 'email':
                                        return /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
                                    case 'tel':
                                        return /^[0-9\-\+]{3,25}$/;
                                    case 'ipaddress':
                                        return /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
                                    case 'double':
                                    case 'float':
                                        return /^-?[0-9]*[.][0-9]+$/;
                                    case 'int':
                                        return /^-?[0-9]+/g;
                                    case 'intpositive':
                                        return /^[0-9]+/g;
                                    case 'hex':
                                        return /0x[A-Fa-f0-9]{2}/;
                                    case 'hexlong':
                                        return /0x[A-Fa-f0-9]/;
                                    case 'stringuppercase':
                                        return /[A-Z]\s+/;
                                    case 'alphanumeric':
                                        return /[a-zA-Z0-9\s]+/;
                                    case 'time':
                                        return /([01]\d{1}|2[0-3]):([0-5]\d{1})/;
                                    default:
                                        return new RegExp(regex);
                                }
                            })(regex);

                            if(!r.test(value)) {
                                invalids.push(this);
                            }
                        }
                    }
                    else if(required) {
                        invalids.push(this);
                    }
                }
            });

            if(invalids.length > 0) {
                e.preventDefault();
                settings.fail.call(form, e, invalids, data);
            } else {
                settings.success.call(form, e, data);
            }
        })
        // Disable default HTML5 validation
        .attr('novalidate', '');

    };

    $.fn.bootstrap3Validate = function(success, data) {
        return this.validate({
            'init': function() {
                $('.has-error', this).removeClass('has-error').find('input,textarea').tooltip('destroy');
                $('.alert', this).hide();
                $('[rel=tooltip]', this).tooltip('destroy');
            },
            'success': function(e, data) {
                if (typeof(success) === 'function') {
                    success.call(this, e, data);
                }
            },
            'fail': function(e, invalids) {
                var form = this;

                $(invalids).closest('.form-group').addClass('has-error').find('input,select,textarea').each(function(i) {
                    var target = $(this);
                    var text = target.attr('data-title');
                    if(!text) {
                        text = target.attr('placeholder');
                    }

                    if(text) {
                        if(!target.is("[type='checkbox']")) {
                            target.tooltip({'trigger':'focus', placement: 'top', title: text});
                        }

                        if(i == 0) {
                            $('.alert-danger', form).show().text(text);
                            this.focus();
                        }
                    }
                });
            },
        });
    }

}( jQuery ));

/* =============================================================
 * bootstrap-combobox.js v1.1.6
 * =============================================================
 * Copyright 2012 Daniel Farrell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============================================================ */

!function( $ ) {

 "use strict";

 /* COMBOBOX PUBLIC CLASS DEFINITION
  * ================================ */

  var Combobox = function ( element, options ) {
    this.options = $.extend({}, $.fn.combobox.defaults, options);
    this.$source = $(element);
    this.$container = this.setup();
    this.$element = this.$container.find('input[type=text]');
    this.$target = this.$container.find('input[type=hidden]');
    this.$button = this.$container.find('.dropdown-toggle');
    this.$menu = $(this.options.menu).appendTo(this.$container);
    this.template = this.options.template || this.template
    this.matcher = this.options.matcher || this.matcher;
    this.sorter = this.options.sorter || this.sorter;
    this.highlighter = this.options.highlighter || this.highlighter;
    this.shown = false;
    this.selected = false;
    this.refresh();
    this.transferAttributes();
    this.listen();
  };

  Combobox.prototype = {

    constructor: Combobox

  , setup: function () {
      var combobox = $(this.template());
      this.$source.before(combobox);
      this.$source.hide();
      return combobox;
    }

  , disable: function() {
      this.$element.prop('disabled', true);
      this.$button.attr('disabled', true);
      this.disabled = true;
      this.$container.addClass('combobox-disabled');
    }

  , enable: function() {
      this.$element.prop('disabled', false);
      this.$button.attr('disabled', false);
      this.disabled = false;
      this.$container.removeClass('combobox-disabled');
    }
  , parse: function () {
      var that = this
        , map = {}
        , source = []
        , selected = false
        , selectedValue = '';
      this.$source.find('option').each(function() {
        var option = $(this);
        if (option.val() === '') {
          that.options.placeholder = option.text();
          that.$element.attr('placeholder', that.options.placeholder);
          return;
        }
        map[option.text()] = option.val();
        source.push(option.text());
        if (option.prop('selected')) {
          selected = option.text();
          selectedValue = option.val();
        }
      })
      this.map = map;
      if (selected) {
        this.$element.val(selected);
        this.$target.val(selectedValue);
        this.$container.addClass('combobox-selected');
        this.selected = true;
      }
      return source;
    }

  , transferAttributes: function() {
    this.options.placeholder = this.$source.attr('data-placeholder') || this.options.placeholder
    this.$element.attr('placeholder', this.options.placeholder)
    this.$target.prop('name', this.$source.prop('name'))
    this.$target.val(this.$source.val())
    this.$source.removeAttr('name')  // Remove from source otherwise form will pass parameter twice.
    this.$element.attr('required', this.$source.attr('required'))
    this.$element.attr('rel', this.$source.attr('rel'))
    this.$element.attr('title', this.$source.attr('title'))
    this.$element.attr('class', this.$source.attr('class'))
    this.$element.attr('tabindex', this.$source.attr('tabindex'))
    this.$source.removeAttr('tabindex')
    if (this.$source.attr('disabled')!==undefined)
      this.disable();
  }

  , select: function () {
      var val = this.$menu.find('.active').attr('data-value');
      this.$element.val(this.updater(val)).trigger('change');
      this.$target.val(this.map[val]).trigger('change');
      this.$source.val(this.map[val]).trigger('change');
      this.$container.addClass('combobox-selected');
      this.selected = true;
      return this.hide();
    }
    
  , updater: function (item) {
      return item;
    }

  , show: function () {
      var pos = $.extend({}, this.$element.position(), {
        height: this.$element[0].offsetHeight
      });

      this.$menu
        .insertAfter(this.$element)
        .css({
          top: pos.top + pos.height
        , left: pos.left
        })
        .show();

      $('.dropdown-menu').on('mousedown', $.proxy(this.scrollSafety, this));

      this.shown = true;
      return this;
    }

  , hide: function () {
      this.$menu.hide();
      $('.dropdown-menu').off('mousedown', $.proxy(this.scrollSafety, this));
      this.$element.on('blur', $.proxy(this.blur, this));
      this.shown = false;
      return this;
    }

  , lookup: function (event) {
      this.query = this.$element.val();
      return this.process(this.source);
    }

  , process: function (items) {
      var that = this;

      items = $.grep(items, function (item) {
        return that.matcher(item);
      })

      items = this.sorter(items);

      if (!items.length) {
        return this.shown ? this.hide() : this;
      }

      return this.render(items.slice(0, this.options.items)).show();
    }

  , template: function() {
      if (this.options.bsVersion == '2') {
        return '<div class="combobox-container"><input type="hidden" /> <div class="input-append"> <input type="text" autocomplete="off" /> <span class="add-on dropdown-toggle" data-dropdown="dropdown"> <span class="caret"/> <i class="icon-remove"/> </span> </div> </div>'
      } else {
        return '<div class="combobox-container"> <input type="hidden" /> <div class="input-group"> <input type="text" autocomplete="off" /> <span class="input-group-addon dropdown-toggle" data-dropdown="dropdown"> <span class="caret" /> <span class="glyphicon glyphicon-remove" /> </span> </div> </div>'
      }
    }

  , matcher: function (item) {
      return ~item.toLowerCase().indexOf(this.query.toLowerCase());
    }

  , sorter: function (items) {
      var beginswith = []
        , caseSensitive = []
        , caseInsensitive = []
        , item;

      while (item = items.shift()) {
        if (!item.toLowerCase().indexOf(this.query.toLowerCase())) {beginswith.push(item);}
        else if (~item.indexOf(this.query)) {caseSensitive.push(item);}
        else {caseInsensitive.push(item);}
      }

      return beginswith.concat(caseSensitive, caseInsensitive);
    }

  , highlighter: function (item) {
      var query = this.query.replace(/[\-\[\]{}()*+?.,\\\^$|#\s]/g, '\\$&');
      return item.replace(new RegExp('(' + query + ')', 'ig'), function ($1, match) {
        return '<strong>' + match + '</strong>';
      })
    }

  , render: function (items) {
      var that = this;

      items = $(items).map(function (i, item) {
        i = $(that.options.item).attr('data-value', item);
        i.find('a').html(that.highlighter(item));
        return i[0];
      })

      items.first().addClass('active');
      this.$menu.html(items);
      return this;
    }

  , next: function (event) {
      var active = this.$menu.find('.active').removeClass('active')
        , next = active.next();

      if (!next.length) {
        next = $(this.$menu.find('li')[0]);
      }

      next.addClass('active');
    }

  , prev: function (event) {
      var active = this.$menu.find('.active').removeClass('active')
        , prev = active.prev();

      if (!prev.length) {
        prev = this.$menu.find('li').last();
      }

      prev.addClass('active');
    }

  , toggle: function () {
    if (!this.disabled) {
      if (this.$container.hasClass('combobox-selected')) {
        this.clearTarget();
        this.triggerChange();
        this.clearElement();
      } else {
        if (this.shown) {
          this.hide();
        } else {
          this.clearElement();
          this.lookup();
        }
      }
    }
  }

  , scrollSafety: function(e) {
      if (e.target.tagName == 'UL') {
          this.$element.off('blur');
      }
  }
  , clearElement: function () {
    this.$element.val('').focus();
  }

  , clearTarget: function () {
    this.$source.val('');
    this.$target.val('');
    this.$container.removeClass('combobox-selected');
    this.selected = false;
  }

  , triggerChange: function () {
    this.$source.trigger('change');
  }

  , refresh: function () {
    this.source = this.parse();
    this.options.items = this.source.length;
    this.triggerChange();
  }

  , listen: function () {
      this.$element
        .on('focus',    $.proxy(this.focus, this))
        .on('blur',     $.proxy(this.blur, this))
        .on('keypress', $.proxy(this.keypress, this))
        .on('keyup',    $.proxy(this.keyup, this));

      if (this.eventSupported('keydown')) {
        this.$element.on('keydown', $.proxy(this.keydown, this));
      }

      this.$menu
        .on('click', $.proxy(this.click, this))
        .on('mouseenter', 'li', $.proxy(this.mouseenter, this))
        .on('mouseleave', 'li', $.proxy(this.mouseleave, this));

      this.$button
        .on('click', $.proxy(this.toggle, this));
    }

  , eventSupported: function(eventName) {
      var isSupported = eventName in this.$element;
      if (!isSupported) {
        this.$element.setAttribute(eventName, 'return;');
        isSupported = typeof this.$element[eventName] === 'function';
      }
      return isSupported;
    }

  , move: function (e) {
      if (!this.shown) {return;}

      switch(e.keyCode) {
        case 9: // tab
        case 13: // enter
        case 27: // escape
          e.preventDefault();
          break;

        case 38: // up arrow
          e.preventDefault();
          this.prev();
          break;

        case 40: // down arrow
          e.preventDefault();
          this.next();
          break;
      }

      e.stopPropagation();
    }

  , keydown: function (e) {
      this.suppressKeyPressRepeat = ~$.inArray(e.keyCode, [40,38,9,13,27]);
      this.move(e);
    }

  , keypress: function (e) {
      if (this.suppressKeyPressRepeat) {return;}
      this.move(e);
    }

  , keyup: function (e) {
      switch(e.keyCode) {
        case 40: // down arrow
        case 39: // right arrow
        case 38: // up arrow
        case 37: // left arrow
        case 36: // home
        case 35: // end
        case 16: // shift
        case 17: // ctrl
        case 18: // alt
          break;

        case 9: // tab
        case 13: // enter
          if (!this.shown) {return;}
          this.select();
          break;

        case 27: // escape
          if (!this.shown) {return;}
          this.hide();
          break;

        default:
          this.clearTarget();
          this.lookup();
      }

      e.stopPropagation();
      e.preventDefault();
  }

  , focus: function (e) {
      this.focused = true;
    }

  , blur: function (e) {
      var that = this;
      this.focused = false;
      var val = this.$element.val();
      if (!this.selected && val !== '' ) {
        this.$element.val('');
        this.$source.val('').trigger('change');
        this.$target.val('').trigger('change');
      }
      if (!this.mousedover && this.shown) {setTimeout(function () { that.hide(); }, 200);}
    }

  , click: function (e) {
      e.stopPropagation();
      e.preventDefault();
      this.select();
      this.$element.focus();
    }

  , mouseenter: function (e) {
      this.mousedover = true;
      this.$menu.find('.active').removeClass('active');
      $(e.currentTarget).addClass('active');
    }

  , mouseleave: function (e) {
      this.mousedover = false;
    }
  };

  /* COMBOBOX PLUGIN DEFINITION
   * =========================== */
  $.fn.combobox = function ( option ) {
    return this.each(function () {
      var $this = $(this)
        , data = $this.data('combobox')
        , options = typeof option == 'object' && option;
      if(!data) {$this.data('combobox', (data = new Combobox(this, options)));}
      if (typeof option == 'string') {data[option]();}
    });
  };

  $.fn.combobox.defaults = {
    bsVersion: '3'
  , menu: '<ul class="typeahead typeahead-long dropdown-menu"></ul>'
  , item: '<li><a href="#"></a></li>'
  };

  $.fn.combobox.Constructor = Combobox;

}( window.jQuery );

/*
* bootstrap-table - v1.8.1 - 2015-05-29
* https://github.com/wenzhixin/bootstrap-table
* Copyright (c) 2015 zhixin wen
* Licensed MIT License
*/
function alphanum(a,b){function c(a){for(var b,c,d=[],e=0,f=-1,g=0;b=(c=a.charAt(e++)).charCodeAt(0);){var h=46===b||b>=48&&57>=b;h!==g&&(d[++f]="",g=h),d[f]+=c}return d}var d=c(a),e=c(b);for(x=0;d[x]&&e[x];x++)if(d[x]!==e[x]){var f=Number(d[x]),g=Number(e[x]);return f==d[x]&&g==e[x]?f-g:d[x]>e[x]?1:-1}return d.length-e.length}!function(a){"use strict";var b=37,c=null,d="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAByUDbMAAAAZ0lEQVQ4y2NgGLKgquEuFxBPAGI2ahhWCsS/gDibUoO0gPgxEP8H4ttArEyuQYxAPBdqEAxPBImTY5gjEL9DM+wTENuQahAvEO9DMwiGdwAxOymGJQLxTyD+jgWDxCMZRsEoGAVoAADeemwtPcZI2wAAAABJRU5ErkJggg==",e="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAQAAADYWf5HAAAAkElEQVQoz7XQMQ5AQBCF4dWQSJxC5wwax1Cq1e7BAdxD5SL+Tq/QCM1oNiJidwox0355mXnG/DrEtIQ6azioNZQxI0ykPhTQIwhCR+BmBYtlK7kLJYwWCcJA9M4qdrZrd8pPjZWPtOqdRQy320YSV17OatFC4euts6z39GYMKRPCTKY9UnPQ6P+GtMRfGtPnBCiqhAeJPmkqAAAAAElFTkSuQmCC",f="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAByUDbMAAAAZUlEQVQ4y2NgGAWjYBSggaqGu5FA/BOIv2PBIPFEUgxjB+IdQPwfC94HxLykus4GiD+hGfQOiB3J8SojEE9EM2wuSJzcsFMG4ttQgx4DsRalkZENxL+AuJQaMcsGxBOAmGvopk8AVz1sLZgg0bsAAAAASUVORK5CYII= ",g=function(a){var b=arguments,c=!0,d=1;return a=a.replace(/%s/g,function(){var a=b[d++];return"undefined"==typeof a?(c=!1,""):a}),c?a:""},h=function(b,c,d,e){var f="";return a.each(b,function(a,b){return b[c]===e?(f=b[d],!1):!0}),f},i=function(b,c){var d=-1;return a.each(b,function(a,b){return b.field===c?(d=a,!1):!0}),d},j=function(){if(null===c){var b,d,e=a("<p/>").addClass("fixed-table-scroll-inner"),f=a("<div/>").addClass("fixed-table-scroll-outer");f.append(e),a("body").append(f),b=e[0].offsetWidth,f.css("overflow","scroll"),d=e[0].offsetWidth,b===d&&(d=f[0].clientWidth),f.remove(),c=b-d}return c},k=function(b,c,d,e){var f=c;if("string"==typeof c){var h=c.split(".");h.length>1?(f=window,a.each(h,function(a,b){f=f[b]})):f=window[c]}return"object"==typeof f?f:"function"==typeof f?f.apply(b,d):!f&&"string"==typeof c&&g.apply(this,[c].concat(d))?g.apply(this,[c].concat(d)):e},l=function(a){return"string"==typeof a?a.replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;").replace(/"/g,"&quot;").replace(/'/g,"&#039;"):a},m=function(b){var c=0;return b.children().each(function(){c<a(this).outerHeight(!0)&&(c=a(this).outerHeight(!0))}),c},n=function(a){for(var b in a){var c=b.split(/(?=[A-Z])/).join("-").toLowerCase();c!==b&&(a[c]=a[b],delete a[b])}return a},o=function(b,c){this.options=c,this.$el=a(b),this.$el_=this.$el.clone(),this.timeoutId_=0,this.timeoutFooter_=0,this.init()};o.DEFAULTS={classes:"table table-hover",height:void 0,undefinedText:"-",sortName:void 0,sortOrder:"asc",striped:!1,columns:[],data:[],method:"get",url:void 0,ajax:void 0,cache:!0,contentType:"application/json",dataType:"json",ajaxOptions:{},queryParams:function(a){return a},queryParamsType:"limit",responseHandler:function(a){return a},pagination:!1,sidePagination:"client",totalRows:0,pageNumber:1,pageSize:10,pageList:[10,25,50,100],paginationHAlign:"right",paginationVAlign:"bottom",paginationDetailHAlign:"left",paginationFirstText:"&laquo;",paginationPreText:"&lsaquo;",paginationNextText:"&rsaquo;",paginationLastText:"&raquo;",search:!1,searchAlign:"right",selectItemName:"btSelectItem",showHeader:!0,showFooter:!1,showColumns:!1,showPaginationSwitch:!1,showRefresh:!1,showToggle:!1,buttonsAlign:"right",smartDisplay:!0,minimumCountColumns:1,idField:void 0,uniqueId:void 0,cardView:!1,detailView:!1,detailFormatter:function(){return""},trimOnSearch:!0,clickToSelect:!1,singleSelect:!1,toolbar:void 0,toolbarAlign:"left",checkboxHeader:!0,sortable:!0,maintainSelected:!1,searchTimeOut:500,searchText:"",iconSize:void 0,iconsPrefix:"glyphicon",icons:{paginationSwitchDown:"glyphicon-collapse-down icon-chevron-down",paginationSwitchUp:"glyphicon-collapse-up icon-chevron-up",refresh:"glyphicon-refresh icon-refresh",toggle:"glyphicon-list-alt icon-list-alt",columns:"glyphicon-th icon-th"},rowStyle:function(){return{}},rowAttributes:function(){return{}},onAll:function(){return!1},onClickCell:function(){return!1},onDblClickCell:function(){return!1},onClickRow:function(){return!1},onDblClickRow:function(){return!1},onSort:function(){return!1},onCheck:function(){return!1},onUncheck:function(){return!1},onCheckAll:function(){return!1},onUncheckAll:function(){return!1},onCheckSome:function(){return!1},onUncheckSome:function(){return!1},onLoadSuccess:function(){return!1},onLoadError:function(){return!1},onColumnSwitch:function(){return!1},onPageChange:function(){return!1},onSearch:function(){return!1},onToggle:function(){return!1},onPreBody:function(){return!1},onPostBody:function(){return!1},onPostHeader:function(){return!1},onExpandRow:function(){return!1},onCollapseRow:function(){return!1}},o.LOCALES=[],o.LOCALES["en-US"]={formatLoadingMessage:function(){return"Loading, please wait..."},formatRecordsPerPage:function(a){return g("%s records per page",a)},formatShowingRows:function(a,b,c){return g("Showing %s to %s of %s rows",a,b,c)},formatSearch:function(){return"Search"},formatNoMatches:function(){return"No matching records found"},formatPaginationSwitch:function(){return"Hide/Show pagination"},formatRefresh:function(){return"Refresh"},formatToggle:function(){return"Toggle"},formatColumns:function(){return"Columns"},formatAllRows:function(){return"All"}},a.extend(o.DEFAULTS,o.LOCALES["en-US"]),o.COLUMN_DEFAULTS={radio:!1,checkbox:!1,checkboxEnabled:!0,field:void 0,title:void 0,"class":void 0,align:void 0,halign:void 0,falign:void 0,valign:void 0,width:void 0,sortable:!1,order:"asc",visible:!0,switchable:!0,clickToSelect:!0,formatter:void 0,footerFormatter:void 0,events:void 0,sorter:void 0,sortName:void 0,cellStyle:void 0,searchable:!0,cardVisible:!0},o.EVENTS={"all.bs.table":"onAll","click-cell.bs.table":"onClickCell","dbl-click-cell.bs.table":"onDblClickCell","click-row.bs.table":"onClickRow","dbl-click-row.bs.table":"onDblClickRow","sort.bs.table":"onSort","check.bs.table":"onCheck","uncheck.bs.table":"onUncheck","check-all.bs.table":"onCheckAll","uncheck-all.bs.table":"onUncheckAll","check-some.bs.table":"onCheckSome","uncheck-some.bs.table":"onUncheckSome","load-success.bs.table":"onLoadSuccess","load-error.bs.table":"onLoadError","column-switch.bs.table":"onColumnSwitch","page-change.bs.table":"onPageChange","search.bs.table":"onSearch","toggle.bs.table":"onToggle","pre-body.bs.table":"onPreBody","post-body.bs.table":"onPostBody","post-header.bs.table":"onPostHeader","expand-row.bs.table":"onExpandRow","collapse-row.bs.table":"onCollapseRow"},o.prototype.init=function(){this.initContainer(),this.initTable(),this.initHeader(),this.initData(),this.initFooter(),this.initToolbar(),this.initPagination(),this.initBody(),this.initServer()},o.prototype.initContainer=function(){this.$container=a(['<div class="bootstrap-table">','<div class="fixed-table-toolbar"></div>',"top"===this.options.paginationVAlign||"both"===this.options.paginationVAlign?'<div class="fixed-table-pagination" style="clear: both;"></div>':"",'<div class="fixed-table-container">','<div class="fixed-table-header"><table></table></div>','<div class="fixed-table-body">','<div class="fixed-table-loading">',this.options.formatLoadingMessage(),"</div>","</div>",'<div class="fixed-table-footer"><table><tr></tr></table></div>',"bottom"===this.options.paginationVAlign||"both"===this.options.paginationVAlign?'<div class="fixed-table-pagination"></div>':"","</div>","</div>"].join("")),this.$container.insertAfter(this.$el),this.$tableContainer=this.$container.find(".fixed-table-container"),this.$tableHeader=this.$container.find(".fixed-table-header"),this.$tableBody=this.$container.find(".fixed-table-body"),this.$tableLoading=this.$container.find(".fixed-table-loading"),this.$tableFooter=this.$container.find(".fixed-table-footer"),this.$toolbar=this.$container.find(".fixed-table-toolbar"),this.$pagination=this.$container.find(".fixed-table-pagination"),this.$tableBody.append(this.$el),this.$container.after('<div class="clearfix"></div>'),this.$el.addClass(this.options.classes),this.options.striped&&this.$el.addClass("table-striped"),-1!==a.inArray("table-no-bordered",this.options.classes.split(" "))&&this.$tableContainer.addClass("table-no-bordered")},o.prototype.initTable=function(){var b=this,c=[],d=[];this.$header=this.$el.find("thead"),this.$header.length||(this.$header=a("<thead></thead>").appendTo(this.$el)),this.$header.find("tr").length||this.$header.append("<tr></tr>"),this.$header.find("th").each(function(){var b=a.extend({},{title:a(this).html(),"class":a(this).attr("class")},a(this).data());c.push(b)}),this.options.columns=a.extend(!0,[],c,this.options.columns),a.each(this.options.columns,function(c,d){b.options.columns[c]=a.extend({},o.COLUMN_DEFAULTS,{field:c},d)}),this.options.data.length||(this.$el.find("tbody tr").each(function(){var c={};c._id=a(this).attr("id"),c._class=a(this).attr("class"),c._data=n(a(this).data()),a(this).find("td").each(function(d){var e=b.options.columns[d].field;c[e]=a(this).html(),c["_"+e+"_id"]=a(this).attr("id"),c["_"+e+"_class"]=a(this).attr("class"),c["_"+e+"_rowspan"]=a(this).attr("rowspan"),c["_"+e+"_data"]=n(a(this).data())}),d.push(c)}),this.options.data=d)},o.prototype.initHeader=function(){var c=this,d=[],e=[];this.header={fields:[],styles:[],classes:[],formatters:[],events:[],sorters:[],sortNames:[],cellStyles:[],clickToSelects:[],searchables:[]},!this.options.cardView&&this.options.detailView&&(e.push('<th class="detail"><div class="fht-cell"></div></th>'),d.push({})),a.each(this.options.columns,function(a,b){var f="",h="",i="",j="",k=g(' class="%s"',b["class"]),l=(c.options.sortOrder||b.order,"px"),m=b.width;return b.visible?void((!c.options.cardView||b.cardVisible)&&(void 0===b.width||c.options.cardView||"string"==typeof b.width&&-1!==b.width.indexOf("%")&&(l="%"),b.width&&"string"==typeof b.width&&(m=b.width.replace("%","").replace("px","")),h=g("text-align: %s; ",b.halign?b.halign:b.align),i=g("text-align: %s; ",b.align),j=g("vertical-align: %s; ",b.valign),j+=g("width: %s%s; ",b.checkbox||b.radio?36:m,l),d.push(b),c.header.fields.push(b.field),c.header.styles.push(i+j),c.header.classes.push(k),c.header.formatters.push(b.formatter),c.header.events.push(b.events),c.header.sorters.push(b.sorter),c.header.sortNames.push(b.sortName),c.header.cellStyles.push(b.cellStyle),c.header.clickToSelects.push(b.clickToSelect),c.header.searchables.push(b.searchable),e.push("<th",b.checkbox||b.radio?g(' class="bs-checkbox %s"',b["class"]||""):k,g(' style="%s"',h+j),">"),e.push(g('<div class="th-inner %s">',c.options.sortable&&b.sortable?"sortable":"")),f=b.title,b.checkbox&&(!c.options.singleSelect&&c.options.checkboxHeader&&(f='<input name="btSelectAll" type="checkbox" />'),c.header.stateField=b.field),b.radio&&(f="",c.header.stateField=b.field,c.options.singleSelect=!0),e.push(f),e.push("</div>"),e.push('<div class="fht-cell"></div>'),e.push("</div>"),e.push("</th>"))):void(b.field===c.options.sortName&&c.header.fields.push(b.field))}),this.$header.find("tr").html(e.join("")),this.$header.find("th").each(function(b){a(this).data(d[b])}),this.$container.off("click",".th-inner").on("click",".th-inner",function(b){c.options.sortable&&a(this).parent().data().sortable&&c.onSort(b)}),!this.options.showHeader||this.options.cardView?(this.$header.hide(),this.$tableHeader.hide(),this.$tableLoading.css("top",0)):(this.$header.show(),this.$tableHeader.show(),this.$tableLoading.css("top",b+"px"),this.getCaretHtml()),this.$selectAll=this.$header.find('[name="btSelectAll"]'),this.$container.off("click",'[name="btSelectAll"]').on("click",'[name="btSelectAll"]',function(){var b=a(this).prop("checked");c[b?"checkAll":"uncheckAll"]()})},o.prototype.initFooter=function(){!this.options.showFooter||this.options.cardView?this.$tableFooter.hide():this.$tableFooter.show()},o.prototype.initData=function(a,b){this.data="append"===b?this.data.concat(a):"prepend"===b?[].concat(a).concat(this.data):a||this.options.data,this.options.data="append"===b?this.options.data.concat(a):"prepend"===b?[].concat(a).concat(this.options.data):this.data,"server"!==this.options.sidePagination&&this.initSort()},o.prototype.initSort=function(){var b=this,c=this.options.sortName,d="desc"===this.options.sortOrder?-1:1,e=a.inArray(this.options.sortName,this.header.fields);-1!==e&&this.data.sort(function(f,g){b.header.sortNames[e]&&(c=b.header.sortNames[e]);var h=f[c],i=g[c],j=k(b.header,b.header.sorters[e],[h,i]);return void 0!==j?d*j:((void 0===h||null===h)&&(h=""),(void 0===i||null===i)&&(i=""),a.isNumeric(h)&&a.isNumeric(i)?(h=parseFloat(h),i=parseFloat(i),i>h?-1*d:d):h===i?0:("string"!=typeof h&&(h=h.toString()),-1===h.localeCompare(i)?-1*d:d))})},o.prototype.onSort=function(b){var c=a(b.currentTarget).parent(),d=this.$header.find("th").eq(c.index());return this.$header.add(this.$header_).find("span.order").remove(),this.options.sortName===c.data("field")?this.options.sortOrder="asc"===this.options.sortOrder?"desc":"asc":(this.options.sortName=c.data("field"),this.options.sortOrder="asc"===c.data("order")?"desc":"asc"),this.trigger("sort",this.options.sortName,this.options.sortOrder),c.add(d).data("order",this.options.sortOrder),this.getCaretHtml(),"server"===this.options.sidePagination?void this.initServer():(this.initSort(),void this.initBody())},o.prototype.initToolbar=function(){var b,c,d=this,e=[],f=0,h=0;this.$toolbar.html(""),"string"==typeof this.options.toolbar&&a(g('<div class="bars pull-%s"></div>',this.options.toolbarAlign)).appendTo(this.$toolbar).append(a(this.options.toolbar)),e=[g('<div class="columns columns-%s btn-group pull-%s">',this.options.buttonsAlign,this.options.buttonsAlign)],"string"==typeof this.options.icons&&(this.options.icons=k(null,this.options.icons)),this.options.showPaginationSwitch&&e.push(g('<button class="btn btn-default" type="button" name="paginationSwitch" title="%s">',this.options.formatPaginationSwitch()),g('<i class="%s %s"></i>',this.options.iconsPrefix,this.options.icons.paginationSwitchDown),"</button>"),this.options.showRefresh&&e.push(g('<button class="btn btn-default'+(void 0===this.options.iconSize?"":" btn-"+this.options.iconSize)+'" type="button" name="refresh" title="%s">',this.options.formatRefresh()),g('<i class="%s %s"></i>',this.options.iconsPrefix,this.options.icons.refresh),"</button>"),this.options.showToggle&&e.push(g('<button class="btn btn-default'+(void 0===this.options.iconSize?"":" btn-"+this.options.iconSize)+'" type="button" name="toggle" title="%s">',this.options.formatToggle()),g('<i class="%s %s"></i>',this.options.iconsPrefix,this.options.icons.toggle),"</button>"),this.options.showColumns&&(e.push(g('<div class="keep-open btn-group" title="%s">',this.options.formatColumns()),'<button type="button" class="btn btn-default'+(void 0==this.options.iconSize?"":" btn-"+this.options.iconSize)+' dropdown-toggle" data-toggle="dropdown">',g('<i class="%s %s"></i>',this.options.iconsPrefix,this.options.icons.columns),' <span class="caret"></span>',"</button>",'<ul class="dropdown-menu" role="menu">'),a.each(this.options.columns,function(a,b){if(!(b.radio||b.checkbox||d.options.cardView&&!b.cardVisible)){var c=b.visible?' checked="checked"':"";b.switchable&&(e.push(g('<li><label><input type="checkbox" data-field="%s" value="%s"%s> %s</label></li>',b.field,a,c,b.title)),h++)}}),e.push("</ul>","</div>")),e.push("</div>"),(this.showToolbar||e.length>2)&&this.$toolbar.append(e.join("")),this.options.showPaginationSwitch&&this.$toolbar.find('button[name="paginationSwitch"]').off("click").on("click",a.proxy(this.togglePagination,this)),this.options.showRefresh&&this.$toolbar.find('button[name="refresh"]').off("click").on("click",a.proxy(this.refresh,this)),this.options.showToggle&&this.$toolbar.find('button[name="toggle"]').off("click").on("click",function(){d.toggleView()}),this.options.showColumns&&(b=this.$toolbar.find(".keep-open"),h<=this.options.minimumCountColumns&&b.find("input").prop("disabled",!0),b.find("li").off("click").on("click",function(a){a.stopImmediatePropagation()}),b.find("input").off("click").on("click",function(){var b=a(this);d.toggleColumn(i(d.options.columns,a(this).data("field")),b.prop("checked"),!1),d.trigger("column-switch",a(this).data("field"),b.prop("checked"))})),this.options.search&&(e=[],e.push('<div class="pull-'+this.options.searchAlign+' search">',g('<input class="form-control'+(void 0===this.options.iconSize?"":" input-"+this.options.iconSize)+'" type="text" placeholder="%s">',this.options.formatSearch()),"</div>"),this.$toolbar.append(e.join("")),c=this.$toolbar.find(".search input"),c.off("keyup drop").on("keyup drop",function(a){clearTimeout(f),f=setTimeout(function(){d.onSearch(a)},d.options.searchTimeOut)}),""!==this.options.searchText&&(c.val(this.options.searchText),clearTimeout(f),f=setTimeout(function(){c.trigger("keyup")},d.options.searchTimeOut)))},o.prototype.onSearch=function(b){var c=a.trim(a(b.currentTarget).val());this.options.trimOnSearch&&a(b.currentTarget).val()!==c&&a(b.currentTarget).val(c),c!==this.searchText&&(this.searchText=c,this.options.pageNumber=1,this.initSearch(),this.updatePagination(),this.trigger("search",c))},o.prototype.initSearch=function(){var b=this;if("server"!==this.options.sidePagination){var c=this.searchText&&this.searchText.toLowerCase(),d=a.isEmptyObject(this.filterColumns)?null:this.filterColumns;this.data=d?a.grep(this.options.data,function(a){for(var b in d)if(a[b]!==d[b])return!1;return!0}):this.options.data,this.data=c?a.grep(this.data,function(d,e){for(var f in d){f=a.isNumeric(f)?parseInt(f,10):f;var g=d[f],h=b.options.columns[i(b.options.columns,f)],j=a.inArray(f,b.header.fields);g=k(h,b.header.formatters[j],[g,d,e],g);var l=a.inArray(f,b.header.fields);if(-1!==l&&b.header.searchables[l]&&("string"==typeof g||"number"==typeof g)&&-1!==(g+"").toLowerCase().indexOf(c))return!0}return!1}):this.data}},o.prototype.initPagination=function(){if(!this.options.pagination)return void this.$pagination.hide();this.$pagination.show();var b,c,d,e,f,h,i,j,k,l=this,m=[],n=!1,o=this.getData();if("server"!==this.options.sidePagination&&(this.options.totalRows=o.length),this.totalPages=0,this.options.totalRows){if(this.options.pageSize===this.options.formatAllRows())this.options.pageSize=this.options.totalRows,n=!0;else if(this.options.pageSize===this.options.totalRows){var p="string"==typeof this.options.pageList?this.options.pageList.replace("[","").replace("]","").replace(/ /g,"").toLowerCase().split(","):this.options.pageList;p.indexOf(this.options.formatAllRows().toLowerCase())>-1&&(n=!0)}this.totalPages=~~((this.options.totalRows-1)/this.options.pageSize)+1,this.options.totalPages=this.totalPages}this.totalPages>0&&this.options.pageNumber>this.totalPages&&(this.options.pageNumber=this.totalPages),this.pageFrom=(this.options.pageNumber-1)*this.options.pageSize+1,this.pageTo=this.options.pageNumber*this.options.pageSize,this.pageTo>this.options.totalRows&&(this.pageTo=this.options.totalRows),m.push('<div class="pull-'+this.options.paginationDetailHAlign+' pagination-detail">','<span class="pagination-info">',this.options.formatShowingRows(this.pageFrom,this.pageTo,this.options.totalRows),"</span>"),m.push('<span class="page-list">');var q=[g('<span class="btn-group %s">',"top"===this.options.paginationVAlign||"both"===this.options.paginationVAlign?"dropdown":"dropup"),'<button type="button" class="btn btn-default '+(void 0===this.options.iconSize?"":" btn-"+this.options.iconSize)+' dropdown-toggle" data-toggle="dropdown">','<span class="page-size">',n?this.options.formatAllRows():this.options.pageSize,"</span>",' <span class="caret"></span>',"</button>",'<ul class="dropdown-menu" role="menu">'],r=this.options.pageList;if("string"==typeof this.options.pageList){var s=this.options.pageList.replace("[","").replace("]","").replace(/ /g,"").split(",");r=[],a.each(s,function(a,b){r.push(b.toUpperCase()===l.options.formatAllRows().toUpperCase()?l.options.formatAllRows():+b)})}for(a.each(r,function(a,b){if(!l.options.smartDisplay||0===a||r[a-1]<=l.options.totalRows){var c;c=n?b===l.options.formatAllRows()?' class="active"':"":b===l.options.pageSize?' class="active"':"",q.push(g('<li%s><a href="javascript:void(0)">%s</a></li>',c,b))}}),q.push("</ul></span>"),m.push(this.options.formatRecordsPerPage(q.join(""))),m.push("</span>"),m.push("</div>",'<div class="pull-'+this.options.paginationHAlign+' pagination">','<ul class="pagination'+(void 0===this.options.iconSize?"":" pagination-"+this.options.iconSize)+'">','<li class="page-first"><a href="javascript:void(0)">'+this.options.paginationFirstText+"</a></li>",'<li class="page-pre"><a href="javascript:void(0)">'+this.options.paginationPreText+"</a></li>"),this.totalPages<5?(c=1,d=this.totalPages):(c=this.options.pageNumber-2,d=c+4,1>c&&(c=1,d=5),d>this.totalPages&&(d=this.totalPages,c=d-4)),b=c;d>=b;b++)m.push('<li class="page-number'+(b===this.options.pageNumber?" active":"")+'">','<a href="javascript:void(0)">',b,"</a>","</li>");m.push('<li class="page-next"><a href="javascript:void(0)">'+this.options.paginationNextText+"</a></li>",'<li class="page-last"><a href="javascript:void(0)">'+this.options.paginationLastText+"</a></li>","</ul>","</div>"),this.$pagination.html(m.join("")),e=this.$pagination.find(".page-list a"),f=this.$pagination.find(".page-first"),h=this.$pagination.find(".page-pre"),i=this.$pagination.find(".page-next"),j=this.$pagination.find(".page-last"),k=this.$pagination.find(".page-number"),this.options.pageNumber<=1&&(f.addClass("disabled"),h.addClass("disabled")),this.options.pageNumber>=this.totalPages&&(i.addClass("disabled"),j.addClass("disabled")),this.options.smartDisplay&&(this.totalPages<=1&&this.$pagination.find("div.pagination").hide(),(r.length<2||this.options.totalRows<=r[0])&&this.$pagination.find("span.page-list").hide(),this.$pagination[this.getData().length?"show":"hide"]()),n&&(this.options.pageSize=this.options.formatAllRows()),e.off("click").on("click",a.proxy(this.onPageListChange,this)),f.off("click").on("click",a.proxy(this.onPageFirst,this)),h.off("click").on("click",a.proxy(this.onPagePre,this)),i.off("click").on("click",a.proxy(this.onPageNext,this)),j.off("click").on("click",a.proxy(this.onPageLast,this)),k.off("click").on("click",a.proxy(this.onPageNumber,this))},o.prototype.updatePagination=function(b){b&&a(b.currentTarget).hasClass("disabled")||(this.options.maintainSelected||this.resetRows(),this.initPagination(),"server"===this.options.sidePagination?this.initServer():this.initBody(),this.trigger("page-change",this.options.pageNumber,this.options.pageSize))},o.prototype.onPageListChange=function(b){var c=a(b.currentTarget);c.parent().addClass("active").siblings().removeClass("active"),this.options.pageSize=c.text().toUpperCase()===this.options.formatAllRows().toUpperCase()?this.options.formatAllRows():+c.text(),this.$toolbar.find(".page-size").text(this.options.pageSize),this.updatePagination(b)},o.prototype.onPageFirst=function(a){this.options.pageNumber=1,this.updatePagination(a)},o.prototype.onPagePre=function(a){this.options.pageNumber--,this.updatePagination(a)},o.prototype.onPageNext=function(a){this.options.pageNumber++,this.updatePagination(a)},o.prototype.onPageLast=function(a){this.options.pageNumber=this.totalPages,this.updatePagination(a)},o.prototype.onPageNumber=function(b){this.options.pageNumber!==+a(b.currentTarget).text()&&(this.options.pageNumber=+a(b.currentTarget).text(),this.updatePagination(b))},o.prototype.initBody=function(b){var c=this,d=[],e=this.getData();this.trigger("pre-body",e),this.$body=this.$el.find("tbody"),this.$body.length||(this.$body=a("<tbody></tbody>").appendTo(this.$el)),this.options.pagination&&"server"!==this.options.sidePagination||(this.pageFrom=1,this.pageTo=e.length);for(var f=this.pageFrom-1;f<this.pageTo;f++){var j,m=e[f],n={},o=[],p="",q={},r=[];if(n=k(this.options,this.options.rowStyle,[m,f],n),n&&n.css)for(j in n.css)o.push(j+": "+n.css[j]);if(q=k(this.options,this.options.rowAttributes,[m,f],q))for(j in q)r.push(g('%s="%s"',j,l(q[j])));m._data&&!a.isEmptyObject(m._data)&&a.each(m._data,function(a,b){"index"!==a&&(p+=g(' data-%s="%s"',a,b))}),d.push("<tr",g(" %s",r.join(" ")),g(' id="%s"',a.isArray(m)?void 0:m._id),g(' class="%s"',n.classes||(a.isArray(m)?void 0:m._class)),g(' data-index="%s"',f),g(' data-uniqueid="%s"',m[this.options.uniqueId]),g("%s",p),">"),this.options.cardView&&d.push(g('<td colspan="%s">',this.header.fields.length)),!this.options.cardView&&this.options.detailView&&d.push("<td>",'<a class="detail-icon" href="javascript:">','<i class="glyphicon glyphicon-plus icon-plus"></i>',"</a>","</td>"),a.each(this.header.fields,function(b,e){var j="",l=m[e],p="",q={},r="",s=c.header.classes[b],t="",u="",v=c.options.columns[i(c.options.columns,e)];if(n=g('style="%s"',o.concat(c.header.styles[b]).join("; ")),l=k(v,c.header.formatters[b],[l,m,f],l),m["_"+e+"_id"]&&(r=g(' id="%s"',m["_"+e+"_id"])),m["_"+e+"_class"]&&(s=g(' class="%s"',m["_"+e+"_class"])),m["_"+e+"_rowspan"]&&(u=g(' rowspan="%s"',m["_"+e+"_rowspan"])),q=k(c.header,c.header.cellStyles[b],[l,m,f],q),q.classes&&(s=g(' class="%s"',q.classes)),q.css){var w=[];for(var x in q.css)w.push(x+": "+q.css[x]);n=g('style="%s"',w.concat(c.header.styles[b]).join("; "))}m["_"+e+"_data"]&&!a.isEmptyObject(m["_"+e+"_data"])&&a.each(m["_"+e+"_data"],function(a,b){"index"!==a&&(t+=g(' data-%s="%s"',a,b))}),v.checkbox||v.radio?(p=v.checkbox?"checkbox":p,p=v.radio?"radio":p,j=[c.options.cardView?'<div class="card-view">':'<td class="bs-checkbox">',"<input"+g(' data-index="%s"',f)+g(' name="%s"',c.options.selectItemName)+g(' type="%s"',p)+g(' value="%s"',m[c.options.idField])+g(' checked="%s"',l===!0||l&&l.checked?"checked":void 0)+g(' disabled="%s"',!v.checkboxEnabled||l&&l.disabled?"disabled":void 0)+" />",c.options.cardView?"</div>":"</td>"].join(""),m[c.header.stateField]=l===!0||l&&l.checked):(l="undefined"==typeof l||null===l?c.options.undefinedText:l,j=c.options.cardView?['<div class="card-view">',c.options.showHeader?g('<span class="title" %s>%s</span>',n,h(c.options.columns,"field","title",e)):"",g('<span class="value">%s</span>',l),"</div>"].join(""):[g("<td%s %s %s %s %s>",r,s,n,t,u),l,"</td>"].join(""),c.options.cardView&&c.options.smartDisplay&&""===l&&(j="")),d.push(j)}),this.options.cardView&&d.push("</td>"),d.push("</tr>")}d.length||d.push('<tr class="no-records-found">',g('<td colspan="%s">%s</td>',this.$header.find("th").length,this.options.formatNoMatches()),"</tr>"),this.$body.html(d.join("")),b||this.scrollTo(0),this.$body.find("> tr > td").off("click").on("click",function(){var b=a(this),d=b.parent(),e=c.data[d.data("index")],f=b[0].cellIndex,h=c.$header.find("th:eq("+f+")"),i=h.data("field"),j=e[i];c.trigger("click-cell",i,j,e,b),c.trigger("click-row",e,d),c.options.clickToSelect&&c.header.clickToSelects[d.children().index(a(this))]&&d.find(g('[name="%s"]',c.options.selectItemName))[0].click()}),this.$body.find("> tr > td").off("dblclick").on("dblclick",function(){var b=a(this),d=b.parent(),e=c.data[d.data("index")],f=b[0].cellIndex,g=c.$header.find("th:eq("+f+")"),h=g.data("field"),i=e[h];c.trigger("dbl-click-cell",h,i,e,b),c.trigger("dbl-click-row",e,d)}),this.$body.find("> tr > td > .detail-icon").off("click").on("click",function(){var b=a(this),d=b.parent().parent(),e=d.data("index"),f=c.options.data[e];d.next().is("tr.detail-view")?(b.find("i").attr("class","glyphicon glyphicon-plus icon-plus"),d.next().remove(),c.trigger("collapse-row",e,f)):(b.find("i").attr("class","glyphicon glyphicon-minus icon-minus"),d.after(g('<tr class="detail-view"><td colspan="%s">%s</td></tr>',d.find("td").length,k(c.options,c.options.detailFormatter,[e,f],""))),c.trigger("expand-row",e,f,d.next().find("td"))),c.resetView()}),this.$selectItem=this.$body.find(g('[name="%s"]',this.options.selectItemName)),this.$selectItem.off("click").on("click",function(b){b.stopImmediatePropagation();var d=a(this).prop("checked"),e=c.data[a(this).data("index")];e[c.header.stateField]=d,c.options.singleSelect&&(c.$selectItem.not(this).each(function(){c.data[a(this).data("index")][c.header.stateField]=!1}),c.$selectItem.filter(":checked").not(this).prop("checked",!1)),c.updateSelected(),c.trigger(d?"check":"uncheck",e)}),a.each(this.header.events,function(b,d){if(d){"string"==typeof d&&(d=k(null,d)),!c.options.cardView&&c.options.detailView&&(b+=1);for(var e in d)c.$body.find("tr").each(function(){var f=a(this),g=f.find(c.options.cardView?".card-view":"td").eq(b),h=e.indexOf(" "),i=e.substring(0,h),j=e.substring(h+1),k=d[e];g.find(j).off(i).on(i,function(a){var d=f.data("index"),e=c.data[d],g=e[c.header.fields[b]];k.apply(this,[a,g,e,d])})})}}),this.updateSelected(),this.resetView(),this.trigger("post-body")},o.prototype.initServer=function(b,c){var d,e=this,f={},g={pageSize:this.options.pageSize===this.options.formatAllRows()?this.options.totalRows:this.options.pageSize,pageNumber:this.options.pageNumber,searchText:this.searchText,sortName:this.options.sortName,sortOrder:this.options.sortOrder};(this.options.url||this.options.ajax)&&("limit"===this.options.queryParamsType&&(g={search:g.searchText,sort:g.sortName,order:g.sortOrder},this.options.pagination&&(g.limit=this.options.pageSize===this.options.formatAllRows()?this.options.totalRows:this.options.pageSize,g.offset=this.options.pageSize===this.options.formatAllRows()?0:this.options.pageSize*(this.options.pageNumber-1))),a.isEmptyObject(this.filterColumnsPartial)||(g.filter=JSON.stringify(this.filterColumnsPartial,null)),f=k(this.options,this.options.queryParams,[g],f),a.extend(f,c||{}),f!==!1&&(b||this.$tableLoading.show(),d=a.extend({},k(null,this.options.ajaxOptions),{type:this.options.method,url:this.options.url,data:"application/json"===this.options.contentType&&"post"===this.options.method?JSON.stringify(f):f,cache:this.options.cache,contentType:this.options.contentType,dataType:this.options.dataType,success:function(a){a=k(e.options,e.options.responseHandler,[a],a),e.load(a),e.trigger("load-success",a)},error:function(a){e.trigger("load-error",a.status)},complete:function(){b||e.$tableLoading.hide()}}),this.options.ajax?k(this,this.options.ajax,[d],null):a.ajax(d)))},o.prototype.getCaretHtml=function(){var b=this;a.each(this.$header.find("th"),function(c,g){a(g).data("field")===b.options.sortName?a(g).find(".sortable").css("background-image","url("+("desc"===b.options.sortOrder?f:d)+")"):a(g).find(".sortable").css("background-image","url("+e+")")})},o.prototype.updateSelected=function(){var b=this.$selectItem.filter(":enabled").length===this.$selectItem.filter(":enabled").filter(":checked").length;this.$selectAll.add(this.$selectAll_).prop("checked",b),this.$selectItem.each(function(){a(this).parents("tr")[a(this).prop("checked")?"addClass":"removeClass"]("selected")})},o.prototype.updateRows=function(){var b=this;this.$selectItem.each(function(){b.data[a(this).data("index")][b.header.stateField]=a(this).prop("checked")})},o.prototype.resetRows=function(){var b=this;a.each(this.data,function(a,c){b.$selectAll.prop("checked",!1),b.$selectItem.prop("checked",!1),c[b.header.stateField]=!1})},o.prototype.trigger=function(b){var c=Array.prototype.slice.call(arguments,1);b+=".bs.table",this.options[o.EVENTS[b]].apply(this.options,c),this.$el.trigger(a.Event(b),c),this.options.onAll(b,c),this.$el.trigger(a.Event("all.bs.table"),[b,c])},o.prototype.resetHeader=function(){clearTimeout(this.timeoutId_),this.timeoutId_=setTimeout(a.proxy(this.fitHeader,this),this.$el.is(":hidden")?100:0)},o.prototype.fitHeader=function(){var b,c,d=this;return d.$el.is(":hidden")?void(d.timeoutFooter_=setTimeout(a.proxy(d.fitHeader,d),100)):(b=this.$tableBody.get(0),c=b.scrollWidth>b.clientWidth&&b.scrollHeight>b.clientHeight+this.$header.height()?j():0,this.$el.css("margin-top",-this.$header.height()),this.$header_=this.$header.clone(!0,!0),this.$selectAll_=this.$header_.find('[name="btSelectAll"]'),this.$tableHeader.css({"margin-right":c}).find("table").css("width",this.$el.css("width")).html("").attr("class",this.$el.attr("class")).append(this.$header_),this.$header.find("th").each(function(b){d.$header_.find("th").eq(b).data(a(this).data())
}),this.$body.find("tr:first-child:not(.no-records-found) > *").each(function(b){d.$header_.find("div.fht-cell").eq(b).width(a(this).innerWidth())}),this.$tableBody.off("scroll").on("scroll",function(){d.$tableHeader.scrollLeft(a(this).scrollLeft())}),void d.trigger("post-header"))},o.prototype.resetFooter=function(){var b=this,c=b.getData(),d=[];this.options.showFooter&&!this.options.cardView&&(!this.options.cardView&&this.options.detailView&&d.push("<td></td>"),a.each(this.options.columns,function(a,e){var f="",h="",i=g(' class="%s"',e["class"]);e.visible&&(!b.options.cardView||e.cardVisible)&&(f=g("text-align: %s; ",e.falign?e.falign:e.align),h=g("vertical-align: %s; ",e.valign),d.push("<td",i,g(' style="%s"',f+h),">"),d.push(k(e,e.footerFormatter,[c],"&nbsp;")||"&nbsp;"),d.push("</td>"))}),this.$tableFooter.find("tr").html(d.join("")),clearTimeout(this.timeoutFooter_),this.timeoutFooter_=setTimeout(a.proxy(this.fitFooter,this),this.$el.is(":hidden")?100:0))},o.prototype.fitFooter=function(){var b,c,d;return clearTimeout(this.timeoutFooter_),this.$el.is(":hidden")?void(this.timeoutFooter_=setTimeout(a.proxy(this.fitFooter,this),100)):(c=this.$el.css("width"),d=c>this.$tableBody.width()?j():0,this.$tableFooter.css({"margin-right":d}).find("table").css("width",c).attr("class",this.$el.attr("class")),b=this.$tableFooter.find("td"),void this.$tableBody.find("tbody tr:first-child:not(.no-records-found) > td").each(function(c){b.eq(c).outerWidth(a(this).outerWidth())}))},o.prototype.toggleColumn=function(a,b,c){if(-1!==a&&(this.options.columns[a].visible=b,this.initHeader(),this.initSearch(),this.initPagination(),this.initBody(),this.options.showColumns)){var d=this.$toolbar.find(".keep-open input").prop("disabled",!1);c&&d.filter(g('[value="%s"]',a)).prop("checked",b),d.filter(":checked").length<=this.options.minimumCountColumns&&d.filter(":checked").prop("disabled",!0)}},o.prototype.toggleRow=function(b,c,d){-1!==b&&a(this.$body[0]).children().filter(g(c?'[data-uniqueid="%s"]':'[data-index="%s"]',b))[d?"show":"hide"]()},o.prototype.resetView=function(a){var c=0;if(a&&a.height&&(this.options.height=a.height),this.$selectAll.prop("checked",this.$selectItem.length>0&&this.$selectItem.length===this.$selectItem.filter(":checked").length),this.options.height){var d=m(this.$toolbar),e=m(this.$pagination),f=this.options.height-d-e;this.$tableContainer.css("height",f+"px")}return this.options.cardView?(this.$el.css("margin-top","0"),void this.$tableContainer.css("padding-bottom","0")):(this.options.showHeader&&this.options.height?(this.$tableHeader.show(),this.resetHeader(),c+=b):(this.$tableHeader.hide(),this.trigger("post-header")),this.options.showFooter&&(this.resetFooter(),this.options.height&&(c+=b)),this.getCaretHtml(),void this.$tableContainer.css("padding-bottom",c+"px"))},o.prototype.getData=function(b){return!this.searchText&&a.isEmptyObject(this.filterColumns)&&a.isEmptyObject(this.filterColumnsPartial)?b?this.options.data.slice(this.pageFrom-1,this.pageTo):this.options.data:b?this.data.slice(this.pageFrom-1,this.pageTo):this.data},o.prototype.load=function(b){var c=!1;"server"===this.options.sidePagination?(this.options.totalRows=b.total,c=b.fixedScroll,b=b.rows):a.isArray(b)||(c=b.fixedScroll,b=b.data),this.initData(b),this.initSearch(),this.initPagination(),this.initBody(c)},o.prototype.append=function(a){this.initData(a,"append"),this.initSearch(),this.initPagination(),this.initBody(!0)},o.prototype.prepend=function(a){this.initData(a,"prepend"),this.initSearch(),this.initPagination(),this.initBody(!0)},o.prototype.remove=function(b){var c,d,e=this.options.data.length;if(b.hasOwnProperty("field")&&b.hasOwnProperty("values")){for(c=e-1;c>=0;c--)d=this.options.data[c],d.hasOwnProperty(b.field)&&-1!==a.inArray(d[b.field],b.values)&&this.options.data.splice(c,1);e!==this.options.data.length&&(this.initSearch(),this.initPagination(),this.initBody(!0))}},o.prototype.removeAll=function(){this.options.data.length>0&&(this.options.data.splice(0,this.options.data.length),this.initSearch(),this.initPagination(),this.initBody(!0))},o.prototype.removeByUniqueId=function(a){var b,c,d=this.options.uniqueId,e=this.options.data.length;for(b=e-1;b>=0;b--)c=this.options.data[b],c.hasOwnProperty(d)&&("string"==typeof c[d]?a=a.toString():"number"==typeof c[d]&&(Number(c[d])===c[d]&&c[d]%1===0?a=parseInt(a):c[d]===Number(c[d])&&0!==c[d]&&(a=parseFloat(a))),c[d]===a&&this.options.data.splice(b,1));e!==this.options.data.length&&(this.initSearch(),this.initPagination(),this.initBody(!0))},o.prototype.insertRow=function(a){a.hasOwnProperty("index")&&a.hasOwnProperty("row")&&(this.data.splice(a.index,0,a.row),this.initSearch(),this.initPagination(),this.initSort(),this.initBody(!0))},o.prototype.updateRow=function(b){b.hasOwnProperty("index")&&b.hasOwnProperty("row")&&(a.extend(this.data[b.index],b.row),this.initSort(),this.initBody(!0))},o.prototype.showRow=function(a){a.hasOwnProperty("index")&&this.toggleRow(a.index,void 0===a.isIdField?!1:!0,!0)},o.prototype.hideRow=function(a){a.hasOwnProperty("index")&&this.toggleRow(a.index,void 0===a.isIdField?!1:!0,!1)},o.prototype.getRowsHidden=function(b){var c=a(this.$body[0]).children().filter(":hidden"),d=0;if(b)for(;d<c.length;d++)a(c[d]).show();return c},o.prototype.mergeCells=function(b){var c,d,e=b.index,f=a.inArray(b.field,this.header.fields),g=b.rowspan||1,h=b.colspan||1,i=this.$body.find("tr"),j=i.eq(e).find("td").eq(f);if(!this.options.cardView&&this.options.detailView&&(f+=1),j=i.eq(e).find("td").eq(f),!(0>e||0>f||e>=this.data.length)){for(c=e;e+g>c;c++)for(d=f;f+h>d;d++)i.eq(c).find("td").eq(d).hide();j.attr("rowspan",g).attr("colspan",h).show()}},o.prototype.updateCell=function(a){a.hasOwnProperty("rowIndex")&&a.hasOwnProperty("fieldName")&&a.hasOwnProperty("fieldValue")&&(this.data[a.rowIndex][a.fieldName]=a.fieldValue,this.initSort(),this.initBody(!0))},o.prototype.getOptions=function(){return this.options},o.prototype.getSelections=function(){var b=this;return a.grep(this.data,function(a){return a[b.header.stateField]})},o.prototype.getAllSelections=function(){var b=this;return a.grep(this.options.data,function(a){return a[b.header.stateField]})},o.prototype.checkAll=function(){this.checkAll_(!0)},o.prototype.uncheckAll=function(){this.checkAll_(!1)},o.prototype.checkAll_=function(a){var b;a||(b=this.getSelections()),this.$selectItem.filter(":enabled").prop("checked",a),this.updateRows(),this.updateSelected(),a&&(b=this.getSelections()),this.trigger(a?"check-all":"uncheck-all",b)},o.prototype.check=function(a){this.check_(!0,a)},o.prototype.uncheck=function(a){this.check_(!1,a)},o.prototype.check_=function(a,b){this.$selectItem.filter(g('[data-index="%s"]',b)).prop("checked",a),this.data[b][this.header.stateField]=a,this.updateSelected(),this.trigger(a?"check":"uncheck",this.data[b])},o.prototype.checkBy=function(a){this.checkBy_(!0,a)},o.prototype.uncheckBy=function(a){this.checkBy_(!1,a)},o.prototype.checkBy_=function(b,c){if(c.hasOwnProperty("field")&&c.hasOwnProperty("values")){var d=this,e=[];a.each(this.options.data,function(f,h){return h.hasOwnProperty(c.field)?void(-1!==a.inArray(h[c.field],c.values)&&(d.$selectItem.filter(g('[data-index="%s"]',f)).prop("checked",b),h[d.header.stateField]=b,e.push(h),d.trigger(b?"check":"uncheck",h))):!1}),this.updateSelected(),this.trigger(b?"check-some":"uncheck-some",e)}},o.prototype.destroy=function(){this.$el.insertBefore(this.$container),a(this.options.toolbar).insertBefore(this.$el),this.$container.next().remove(),this.$container.remove(),this.$el.html(this.$el_.html()).css("margin-top","0").attr("class",this.$el_.attr("class")||"")},o.prototype.showLoading=function(){this.$tableLoading.show()},o.prototype.hideLoading=function(){this.$tableLoading.hide()},o.prototype.togglePagination=function(){this.options.pagination=!this.options.pagination;var a=this.$toolbar.find('button[name="paginationSwitch"] i');this.options.pagination?a.attr("class",this.options.iconsPrefix+" "+this.options.icons.paginationSwitchDown):a.attr("class",this.options.iconsPrefix+" "+this.options.icons.paginationSwitchUp),this.updatePagination()},o.prototype.refresh=function(a){a&&a.url&&(this.options.url=a.url,this.options.pageNumber=1),this.initServer(a&&a.silent,a&&a.query)},o.prototype.resetWidth=function(){this.options.showHeader&&this.options.height&&this.fitHeader(),this.options.showFooter&&this.fitFooter()},o.prototype.showColumn=function(a){this.toggleColumn(i(this.options.columns,a),!0,!0)},o.prototype.hideColumn=function(a){this.toggleColumn(i(this.options.columns,a),!1,!0)},o.prototype.filterBy=function(b){this.filterColumns=a.isEmptyObject(b)?{}:b,this.options.pageNumber=1,this.initSearch(),this.updatePagination()},o.prototype.scrollTo=function(a){return"string"==typeof a&&(a="bottom"===a?this.$tableBody[0].scrollHeight:0),"number"==typeof a&&this.$tableBody.scrollTop(a),"undefined"==typeof a?this.$tableBody.scrollTop():void 0},o.prototype.getScrollPosition=function(){return this.scrollTo()},o.prototype.selectPage=function(a){a>0&&a<=this.options.totalPages&&(this.options.pageNumber=a,this.updatePagination())},o.prototype.prevPage=function(){this.options.pageNumber>1&&(this.options.pageNumber--,this.updatePagination())},o.prototype.nextPage=function(){this.options.pageNumber<this.options.totalPages&&(this.options.pageNumber++,this.updatePagination())},o.prototype.toggleView=function(){this.options.cardView=!this.options.cardView,this.initHeader(),this.initBody(),this.trigger("toggle",this.options.cardView)};var p=["getOptions","getSelections","getAllSelections","getData","load","append","prepend","remove","removeAll","insertRow","updateRow","updateCell","removeByUniqueId","showRow","hideRow","getRowsHidden","mergeCells","checkAll","uncheckAll","check","uncheck","checkBy","uncheckBy","refresh","resetView","resetWidth","destroy","showLoading","hideLoading","showColumn","hideColumn","filterBy","scrollTo","getScrollPosition","selectPage","prevPage","nextPage","togglePagination","toggleView"];a.fn.bootstrapTable=function(b){var c,d=Array.prototype.slice.call(arguments,1);return this.each(function(){var e=a(this),f=e.data("bootstrap.table"),g=a.extend({},o.DEFAULTS,e.data(),"object"==typeof b&&b);if("string"==typeof b){if(a.inArray(b,p)<0)throw new Error("Unknown method: "+b);if(!f)return;c=f[b].apply(f,d),"destroy"===b&&e.removeData("bootstrap.table")}f||e.data("bootstrap.table",f=new o(this,g))}),"undefined"==typeof c?this:c},a.fn.bootstrapTable.Constructor=o,a.fn.bootstrapTable.defaults=o.DEFAULTS,a.fn.bootstrapTable.columnDefaults=o.COLUMN_DEFAULTS,a.fn.bootstrapTable.locales=o.LOCALES,a.fn.bootstrapTable.methods=p,a(function(){a('[data-toggle="table"]').bootstrapTable()})}(jQuery),function(a){"use strict";var b={sortOrder:"bs.table.sortOrder",sortName:"bs.table.sortName",pageNumber:"bs.table.pageNumber",pageList:"bs.table.pageList",columns:"bs.table.columns",searchText:"bs.table.searchText"},c=function(){return navigator.cookieEnabled?!0:!1},d=function(a,b,d,e,f,g){if(a.options.stateSave&&c()&&""!==a.options.stateSaveIdTable){var i=a.options.stateSaveIdTable,j=a.options.stateSaveExpire;return b=i+"."+b,!b||/^(?:expires|max\-age|path|domain|secure)$/i.test(b)?!1:(document.cookie=encodeURIComponent(b)+"="+encodeURIComponent(d)+h(j)+(f?"; domain="+f:"")+(e?"; path="+e:"")+(g?"; secure":""),!0)}},e=function(a,b){return b=a+"."+b,b?decodeURIComponent(document.cookie.replace(new RegExp("(?:(?:^|.*;)\\s*"+encodeURIComponent(b).replace(/[\-\.\+\*]/g,"\\$&")+"\\s*\\=\\s*([^;]*).*$)|^.*$"),"$1"))||null:null},f=function(a){return a?new RegExp("(?:^|;\\s*)"+encodeURIComponent(a).replace(/[\-\.\+\*]/g,"\\$&")+"\\s*\\=").test(document.cookie):!1},g=function(a,b,c,d){return b=a+"."+b,f(b)?(document.cookie=encodeURIComponent(b)+"=; expires=Thu, 01 Jan 1970 00:00:00 GMT"+(d?"; domain="+d:"")+(c?"; path="+c:""),!0):!1},h=function(a){var b=a.replace(/[0-9]/,"");switch(a=a.replace(/[A-Za-z]/,""),b.toLowerCase()){case"s":a=+a;break;case"mi":a=60*a;break;case"h":a=60*a*60;break;case"d":a=24*a*60*60;break;case"m":a=30*a*24*60*60;break;case"y":a=365*a*30*24*60*60;break;default:a=void 0}return void 0===a?"":"; max-age="+a};a.extend(a.fn.bootstrapTable.defaults,{stateSave:!1,stateSaveExpire:"2h",stateSaveIdTable:""}),a.fn.bootstrapTable.methods.push("deleteCookie");var i=a.fn.bootstrapTable.Constructor,j=i.prototype.initTable,k=i.prototype.onSort,l=i.prototype.onPageNumber,m=i.prototype.onPageListChange,n=i.prototype.onPageFirst,o=i.prototype.onPagePre,p=i.prototype.onPageNext,q=i.prototype.onPageLast,r=i.prototype.toggleColumn,s=i.prototype.onSearch;i.prototype.initTable=function(){j.apply(this,Array.prototype.slice.apply(arguments)),this.initStateSave()},i.prototype.initStateSave=function(){if(this.options.stateSave&&c()&&""!==this.options.stateSaveIdTable){var d=e(this.options.stateSaveIdTable,b.sortOrder),f=e(this.options.stateSaveIdTable,b.sortName),g=e(this.options.stateSaveIdTable,b.pageNumber),h=e(this.options.stateSaveIdTable,b.pageList),i=JSON.parse(e(this.options.stateSaveIdTable,b.columns)),j=e(this.options.stateSaveIdTable,b.searchText);d&&(this.options.sortOrder=d,this.options.sortName=f),g&&(this.options.pageNumber=+g),h&&(this.options.pageSize=h===this.options.formatAllRows()?h:+h),i&&a.each(this.options.columns,function(a,b){b.visible=-1!==i.indexOf(a)}),j&&(this.options.searchText=j)}},i.prototype.onSort=function(){k.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.sortOrder,this.options.sortOrder),d(this,b.sortName,this.options.sortName)},i.prototype.onPageNumber=function(){l.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageNumber,this.options.pageNumber)},i.prototype.onPageListChange=function(){m.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageList,this.options.pageSize)},i.prototype.onPageFirst=function(){n.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageNumber,this.options.pageNumber)},i.prototype.onPagePre=function(){o.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageNumber,this.options.pageNumber)},i.prototype.onPageNext=function(){p.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageNumber,this.options.pageNumber)},i.prototype.onPageLast=function(){q.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.pageNumber,this.options.pageNumber)},i.prototype.toggleColumn=function(){r.apply(this,Array.prototype.slice.apply(arguments));var c=[];a.each(this.options.columns,function(a){this.visible&&c.push(a)}),d(this,b.columns,JSON.stringify(c))},i.prototype.onSearch=function(){s.apply(this,Array.prototype.slice.apply(arguments)),d(this,b.searchText,this.searchText)},i.prototype.deleteCookie=function(a){""!==a&&c()&&g(b[a])}}(jQuery),!function(a){"use strict";a.extend(a.fn.bootstrapTable.defaults,{editable:!0,onEditableInit:function(){return!1},onEditableSave:function(){return!1}}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"editable-init.bs.table":"onEditableInit","editable-save.bs.table":"onEditableSave"});var b=a.fn.bootstrapTable.Constructor,c=b.prototype.initTable,d=b.prototype.initBody;b.prototype.initTable=function(){var b=this;c.apply(this,Array.prototype.slice.apply(arguments)),this.options.editable&&a.each(this.options.columns,function(a,c){if(c.editable){var d=c.formatter;c.formatter=function(a,e,f){var g=d?d(a,e,f):a;return['<a href="javascript:void(0)"',' data-name="'+c.field+'"',' data-pk="'+e[b.options.idField]+'"',' data-value="'+g+'"',"></a>"].join("")}}})},b.prototype.initBody=function(){var b=this;d.apply(this,Array.prototype.slice.apply(arguments)),this.options.editable&&(a.each(this.options.columns,function(c,d){d.editable&&b.$body.find('a[data-name="'+d.field+'"]').editable(d.editable).off("save").on("save",function(c,e){var f=b.getData(),g=a(this).parents("tr[data-index]").data("index"),h=f[g],i=h[d.field];h[d.field]=e.submitValue,b.trigger("editable-save",d.field,h,i,a(this))})}),this.trigger("editable-init"))}}(jQuery),function(a){"use strict";var b={json:"JSON",xml:"XML",png:"PNG",csv:"CSV",txt:"TXT",sql:"SQL",doc:"MS-Word",excel:"Ms-Excel",powerpoint:"Ms-Powerpoint",pdf:"PDF"};a.extend(a.fn.bootstrapTable.defaults,{showExport:!1,exportTypes:["json","xml","csv","txt","sql","excel"],exportOptions:{}});var c=a.fn.bootstrapTable.Constructor,d=c.prototype.initToolbar;c.prototype.initToolbar=function(){if(this.showToolbar=this.options.showExport,d.apply(this,Array.prototype.slice.apply(arguments)),this.options.showExport){var c=this,e=this.$toolbar.find(">.btn-group"),f=e.find("div.export");if(!f.length){f=a(['<div class="export btn-group">','<button class="btn btn-default dropdown-toggle" data-toggle="dropdown" type="button">','<i class="glyphicon glyphicon-export icon-share"></i> ','<span class="caret"></span>',"</button>",'<ul class="dropdown-menu" role="menu">',"</ul>","</div>"].join("")).appendTo(e);var g=f.find(".dropdown-menu"),h=this.options.exportTypes;if("string"==typeof this.options.exportTypes){var i=this.options.exportTypes.slice(1,-1).replace(/ /g,"").split(",");h=[],a.each(i,function(a,b){h.push(b.slice(1,-1))})}a.each(h,function(a,c){b.hasOwnProperty(c)&&g.append(['<li data-type="'+c+'">','<a href="javascript:void(0)">',b[c],"</a>","</li>"].join(""))}),g.find("li").click(function(){c.$el.tableExport(a.extend({},c.options.exportOptions,{type:a(this).data("type"),escape:!1}))})}}}}(jQuery),!function(a){"use strict";var b=function(a){var b=arguments,c=!0,d=1;return a=a.replace(/%s/g,function(){var a=b[d++];return"undefined"==typeof a?(c=!1,""):a}),c?a:""},c=function(b,c){var d=-1;return a.each(b,function(a,b){return b.field===c?(d=a,!1):!0}),d},d=function(b,c,d,e){if("string"==typeof c){var f=c.split(".");f.length>1?(c=window,a.each(f,function(a,b){c=c[b]})):c=window[c]}return"object"==typeof c?c:"function"==typeof c?c.apply(b,d):e};a.extend(a.fn.bootstrapTable.defaults,{filterControl:!1,onColumnSearch:function(){return!1}}),a.extend(a.fn.bootstrapTable.COLUMN_DEFAULTS,{filterControl:void 0,filterData:void 0}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"column-search.bs.table":"onColumnSearch"});var e=a.fn.bootstrapTable.Constructor,f=e.prototype.initHeader,g=e.prototype.initBody,h=e.prototype.initSearch;e.prototype.initHeader=function(){if(f.apply(this,Array.prototype.slice.apply(arguments)),this.options.filterControl){var c,d,e=!1,g=this,h=0;a.each(this.options.columns,function(f,h){if(c="hidden",d=[],h.visible){if(h.filterControl)switch(d.push('<div style="margin: 0px 2px 2px 2px;" class="filterControl">'),h.filterControl&&h.searchable&&(e=!0,c="visible"),h.filterControl.toLowerCase()){case"input":d.push(b('<input type="text" class="form-control" style="width: 100%; visibility: %s">',c));break;case"select":d.push(b('<select class="%s form-control" style="width: 100%; visibility: %s"></select>',h.field,c))}else d.push('<div style="height: 34px;"></div>');if(g.$header.find(b('.th-inner:eq("%s")',f)).next().append(d.join("")),void 0!==h.filterData&&"column"!==h.filterData.toLowerCase()){var i=h.filterData.substring(0,3),j=h.filterData.substring(4,h.filterData.length),k=a("."+h.field);switch(k.append(a("<option></option>").attr("value","").text("")),i){case"url":a.ajax({url:j,dataType:"json",success:function(b){a.each(b,function(b,c){k.append(a("<option></option>").attr("value",b).text(c))})}});break;case"var":var l=window[j];for(var m in l)k.append(a("<option></option>").attr("value",m).text(l[m]))}}}}),e?(this.$header.off("keyup","input").on("keyup","input",function(a){clearTimeout(h),h=setTimeout(function(){g.onColumnSearch(a)},g.options.searchTimeOut)}),this.$header.off("change","select").on("change","select",function(a){clearTimeout(h),h=setTimeout(function(){g.onColumnSearch(a)},g.options.searchTimeOut)})):this.$header.find(".filterControl").hide()}},e.prototype.initBody=function(){g.apply(this,Array.prototype.slice.apply(arguments));for(var b=this,e=this.getData(),f=this.pageFrom-1;f<this.pageTo;f++){var h=e[f];a.each(this.header.fields,function(e,g){var i=h[g],j=b.options.columns[c(b.options.columns,g)];if(i=d(b.header,b.header.formatters[e],[i,h,f],i),!(j.checkbox&&j.radio||void 0===j.filterControl||"select"!==j.filterControl.toLowerCase()||!j.searchable||void 0!==j.filterData&&"column"!==j.filterData.toLowerCase())){var k,l=a("."+j.field),m=0,n=!1;if(void 0!==l)if(k=l.get(0).options,0===k.length)l.append(a("<option></option>").attr("value","").text("")),l.append(a("<option></option>").attr("value",i).text(i));else{for(;m<k.length;m++)if(k[m].value===i){n=!0;break}n||l.append(a("<option></option>").attr("value",i).text(i))}}})}},e.prototype.initSearch=function(){h.apply(this,Array.prototype.slice.apply(arguments));var b=this,c=a.isEmptyObject(this.filterColumnsPartial)?null:this.filterColumnsPartial;this.data=c?a.grep(this.data,function(e,f){for(var g in c){var h=c[g].toLowerCase(),i=e[g];if(i=d(b.header,b.header.formatters[a.inArray(g,b.header.fields)],[i,e,f],i),-1===a.inArray(g,b.header.fields)||"string"!=typeof i&&"number"!=typeof i||-1===(i+"").toLowerCase().indexOf(h))return!1}return!0}):this.data},e.prototype.onColumnSearch=function(b){var c=a.trim(a(b.currentTarget).val()),d=a(b.currentTarget).parent().parent().parent().data("field");a.isEmptyObject(this.filterColumnsPartial)&&(this.filterColumnsPartial={}),c?this.filterColumnsPartial[d]=c:delete this.filterColumnsPartial[d],this.options.pageNumber=1,this.onSearch(b),this.updatePagination(),this.trigger("column-search",d,c)}}(jQuery),!function(a){"use strict";a.extend(a.fn.bootstrapTable.defaults,{showFilter:!1});var b=a.fn.bootstrapTable.Constructor,c=b.prototype.init,d=b.prototype.initSearch;b.prototype.init=function(){c.apply(this,Array.prototype.slice.apply(arguments));var b=this;this.$el.on("load-success.bs.table",function(){b.options.showFilter&&a(b.options.toolbar).bootstrapTableFilter({connectTo:b.$el})})},b.prototype.initSearch=function(){d.apply(this,Array.prototype.slice.apply(arguments)),"server"!==this.options.sidePagination&&"function"==typeof this.searchCallback&&(this.data=a.grep(this.options.data,this.searchCallback))},b.prototype.getData=function(){return this.searchText||this.searchCallback?this.data:this.options.data},b.prototype.getColumns=function(){return this.options.columns},b.prototype.registerSearchCallback=function(a){this.searchCallback=a},b.prototype.updateSearch=function(){this.options.pageNumber=1,this.initSearch(),this.updatePagination()},b.prototype.getServerUrl=function(){return"server"===this.options.sidePagination?this.options.url:!1},a.fn.bootstrapTable.methods.push("getColumns","registerSearchCallback","updateSearch","getServerUrl")}(jQuery),function(a){"use strict";a.extend(a.fn.bootstrapTable.defaults,{flat:!1});var b=a.fn.bootstrapTable.Constructor,c=b.prototype.initData;b.prototype.initData=function(a,b){this.options.flat&&(a=void 0===a?this.options.data:a,a=d.flatHelper(a)),c.apply(this,[a,b])};var d={flat:function(b){function c(b,e){if(Object(b)!==b)d[e]=b;else if(a.isArray(b))for(var f=0,g=b.length;g>f;f++)c(b[f],e?e+"."+f:""+f),0==g&&(d[e]=[]);else{var h=!0;for(var i in b)h=!1,c(b[i],e?e+"."+i:i);h&&(d[e]={})}}var d={};return c(b,""),d},flatHelper:function(b){var c=[],e=[];return a.isArray(b)||(e.push(b),b=e),a.each(b,function(a,b){c.push(d.flat(b))}),c}}}(jQuery),!function(a){"use strict";a.extend(a.fn.bootstrapTable.defaults,{keyEvents:!1});var b=a.fn.bootstrapTable.Constructor,c=b.prototype.init;b.prototype.init=function(){c.apply(this,Array.prototype.slice.apply(arguments)),this.initKeyEvents()},b.prototype.initKeyEvents=function(){if(this.options.keyEvents){var b=this;a(document).off("keydown").on("keydown",function(a){var c=b.$toolbar.find(".search input"),d=b.$toolbar.find('button[name="refresh"]'),e=b.$toolbar.find('button[name="toggle"]'),f=b.$toolbar.find('button[name="paginationSwitch"]');if(document.activeElement===c.get(0))return!0;switch(a.keyCode){case 83:if(!b.options.search)return;return c.focus(),!1;case 82:if(!b.options.showRefresh)return;return d.click(),!1;case 84:if(!b.options.showToggle)return;return e.click(),!1;case 80:if(!b.options.showPaginationSwitch)return;return f.click(),!1;case 37:if(!b.options.pagination)return;return b.prevPage(),!1;case 39:if(!b.options.pagination)return;return void b.nextPage()}})}}}(jQuery),!function(a){"use strict";var b=function(a){(a.options.height||a.options.showFooter)&&setTimeout(a.resetView(),1)},c=function(a,c,h){a.options.minHeight?d(c,a.options.minWidth)&&d(h,a.options.minHeight)?f(a):e(c,a.options.minWidth)&&e(h,a.options.minHeight)&&g(a):d(c,a.options.minWidth)?f(a):e(c,a.options.minWidth)&&g(a),b(a)},d=function(a,b){return b>=a},e=function(a,b){return a>b},f=function(a){h(a,!1)},g=function(a){h(a,!0)},h=function(a,b){a.options.cardView=b,a.toggleView()};a.extend(a.fn.bootstrapTable.defaults,{mobileResponsive:!1,minWidth:562,minHeight:void 0,checkOnInit:!0,toggled:!1});var i=a.fn.bootstrapTable.Constructor,j=i.prototype.init;i.prototype.init=function(){if(j.apply(this,Array.prototype.slice.apply(arguments)),this.options.mobileResponsive&&this.options.minWidth){var b=this;a(window).resize(function(){c(b,a(this).width(),a(this).height())}),this.options.checkOnInit&&c(this,a(window).width(),a(window).height())}}}(jQuery),function(a){"use strict";var b=!1,c={asc:"Ascending",desc:"Descending"},d="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAByUDbMAAAAZ0lEQVQ4y2NgGLKgquEuFxBPAGI2ahhWCsS/gDibUoO0gPgxEP8H4ttArEyuQYxAPBdqEAxPBImTY5gjEL9DM+wTENuQahAvEO9DMwiGdwAxOymGJQLxTyD+jgWDxCMZRsEoGAVoAADeemwtPcZI2wAAAABJRU5ErkJggg==",e="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABMAAAATCAYAAAByUDbMAAAAZUlEQVQ4y2NgGAWjYBSggaqGu5FA/BOIv2PBIPFEUgxjB+IdQPwfC94HxLykus4GiD+hGfQOiB3J8SojEE9EM2wuSJzcsFMG4ttQgx4DsRalkZENxL+AuJQaMcsGxBOAmGvopk8AVz1sLZgg0bsAAAAASUVORK5CYII= ",f=function(b){if(!a("#sortModal").hasClass("modal")){var c='  <div class="modal fade" id="sortModal" tabindex="-1" role="dialog" aria-labelledby="sortModalLabel" aria-hidden="true">';c+='         <div class="modal-dialog">',c+='             <div class="modal-content">',c+='                 <div class="modal-header">',c+='                     <button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',c+='                     <h4 class="modal-title" id="sortModalLabel">'+b.options.formatMultipleSort()+"</h4>",c+="                 </div>",c+='                 <div class="modal-body">',c+='                     <div class="bootstrap-table">',c+='                         <div class="fixed-table-toolbar">',c+='                             <div class="bars">',c+='                                 <div id="toolbar">',c+='                                     <button id="add" type="button" class="btn btn-default"><i class="'+b.options.iconsPrefix+" "+b.options.icons.plus+'"></i> '+b.options.formatAddLevel()+"</button>",c+='                                     <button id="delete" type="button" class="btn btn-default" disabled><i class="'+b.options.iconsPrefix+" "+b.options.icons.minus+'"></i> '+b.options.formatDeleteLevel()+"</button>",c+="                                 </div>",c+="                             </div>",c+="                         </div>",c+='                         <div class="fixed-table-container">',c+='                             <table id="multi-sort" class="table">',c+="                                 <thead>",c+="                                     <tr>",c+="                                         <th></th>",c+='                                         <th><div class="th-inner">'+b.options.formatColumn()+"</div></th>",c+='                                         <th><div class="th-inner">'+b.options.formatOrder()+"</div></th>",c+="                                     </tr>",c+="                                 </thead>",c+="                                 <tbody></tbody>",c+="                             </table>",c+="                         </div>",c+="                     </div>",c+="                 </div>",c+='                 <div class="modal-footer">',c+='                     <button type="button" class="btn btn-default" data-dismiss="modal">'+b.options.formatCancel()+"</button>",c+='                     <button type="button" class="btn btn-primary">'+b.options.formatSort()+"</button>",c+="                 </div>",c+="             </div>",c+="         </div>",c+="     </div>",a("body").append(a(c));var d=a("#sortModal"),e=d.find("tbody > tr");if(d.off("click","#add").on("click","#add",function(){var a=d.find(".multi-sort-name:first option").length,c=d.find("tbody tr").length;a>c&&(c++,b.addLevel(),b.setButtonStates())}),d.off("click","#delete").on("click","#delete",function(){var a=d.find(".multi-sort-name:first option").length,c=d.find("tbody tr").length;c>1&&a>=c&&(c--,d.find("tbody tr:last").remove(),b.setButtonStates())}),d.off("click",".btn-primary").on("click",".btn-primary",function(){var c=d.find("tbody > tr"),e=d.find("div.alert"),f=[],g=[];b.options.sortPriority=a.map(c,function(b){var c=a(b),d=c.find(".multi-sort-name").val(),e=c.find(".multi-sort-order").val();return f.push(d),{sortName:d,sortOrder:e}});for(var h=f.sort(),i=0;i<f.length-1;i++)h[i+1]==h[i]&&g.push(h[i]);g.length>0?0===e.length&&(e='<div class="alert alert-danger" role="alert"><strong>'+b.options.formatDuplicateAlertTitle()+"</strong> "+b.options.formatDuplicateAlertDescription()+"</div>",a(e).insertBefore(d.find(".bars"))):(1===e.length&&a(e).remove(),b.options.sortName="",b.onMultipleSort(),d.modal("hide"))}),null===b.options.sortPriority&&b.options.sortName&&(b.options.sortPriority=[{sortName:b.options.sortName,sortOrder:b.options.sortOrder}]),null!==b.options.sortPriority){if(e.length<b.options.sortPriority.length&&"object"==typeof b.options.sortPriority)for(var f=0;f<b.options.sortPriority.length;f++)b.addLevel(f,b.options.sortPriority[f])}else b.addLevel(0);b.setButtonStates()}};a.extend(a.fn.bootstrapTable.defaults,{showMultiSort:!1,sortPriority:null,onMultipleSort:function(){return!1}}),a.extend(a.fn.bootstrapTable.defaults.icons,{sort:"glyphicon-sort",plus:"glyphicon-plus",minus:"glyphicon-minus"}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"multiple-sort.bs.table":"onMultipleSort"}),a.extend(a.fn.bootstrapTable.locales,{formatMultipleSort:function(){return"Multiple Sort"},formatAddLevel:function(){return"Add Level"},formatDeleteLevel:function(){return"Delete Level"},formatColumn:function(){return"Column"},formatOrder:function(){return"Order"},formatSortBy:function(){return"Sort by"},formatThenBy:function(){return"Then by"},formatSort:function(){return"Sort"},formatCancel:function(){return"Cancel"},formatDuplicateAlertTitle:function(){return"Duplicate(s) detected!"},formatDuplicateAlertDescription:function(){return"Please remove or change any duplicate column."}}),a.extend(a.fn.bootstrapTable.defaults,a.fn.bootstrapTable.locales);var g=a.fn.bootstrapTable.Constructor,h=g.prototype.initToolbar;g.prototype.initToolbar=function(){this.showToolbar=!0;var c=this;if(h.apply(this,Array.prototype.slice.apply(arguments)),this.options.showMultiSort){var d=this.$toolbar.find(">.btn-group"),e=d.find("div.multi-sort");e.length||(e='  <button class="multi-sort btn btn-default'+(void 0===this.options.iconSize?"":" btn-"+this.options.iconSize)+'" type="button" data-toggle="modal" data-target="#sortModal" title="'+this.options.formatMultipleSort()+'">',e+='     <i class="'+this.options.iconsPrefix+" "+this.options.icons.sort+'"></i>',e+="</button>",d.append(e),f(c)),this.$el.one("sort.bs.table",function(){b=!0}),this.$el.on("multiple-sort.bs.table",function(){b=!1}),this.$el.on("load-success.bs.table",function(){b||null===c.options.sortPriority||"object"!=typeof c.options.sortPriority||c.onMultipleSort()}),this.$el.on("column-switch.bs.table",function(){c.options.sortPriority=null,a("#sortModal").remove(),f(c)})}},g.prototype.onMultipleSort=function(){var b=this,c=function(a,b){return a>b?1:b>a?-1:0
},d=function(d,e){for(var f=[],g=[],h=0;h<b.options.sortPriority.length;h++){var i="desc"===b.options.sortPriority[h].sortOrder?-1:1,j=d[b.options.sortPriority[h].sortName],k=e[b.options.sortPriority[h].sortName];(void 0===j||null===j)&&(j=""),(void 0===k||null===k)&&(k=""),a.isNumeric(j)&&a.isNumeric(k)&&(j=parseFloat(j),k=parseFloat(k)),"string"!=typeof j&&(j=j.toString()),f.push(i*c(j,k)),g.push(i*c(k,j))}return c(f,g)};this.data.sort(function(a,b){return d(a,b)}),this.initBody(),this.assignSortableArrows(),this.trigger("multiple-sort")},g.prototype.addLevel=function(b,d){var e=a("#sortModal"),f=0===b?this.options.formatSortBy():this.options.formatThenBy();e.find("tbody").append(a("<tr>").append(a("<td>").text(f)).append(a("<td>").append(a('<select class="form-control multi-sort-name">'))).append(a("<td>").append(a('<select class="form-control multi-sort-order">'))));var g=e.find(".multi-sort-name").last(),h=e.find(".multi-sort-order").last();this.options.columns.forEach(function(a){return a.sortable===!1||a.visible===!1?!0:void g.append('<option value="'+a.field+'">'+a.title+"</option>")}),a.each(c,function(a,b){h.append('<option value="'+a+'">'+b+"</option>")}),void 0!==d&&(g.find('option[value="'+d.sortName+'"]').attr("selected",!0),h.find('option[value="'+d.sortOrder+'"]').attr("selected",!0))},g.prototype.assignSortableArrows=function(){for(var b=this,c=b.$header.find("th"),f=0;f<c.length;f++)for(var g=0;g<b.options.sortPriority.length;g++)a(c[f]).data("field")===b.options.sortPriority[g].sortName&&a(c[f]).find(".sortable").css("background-image","url("+("desc"===b.options.sortPriority[g].sortOrder?e:d)+")")},g.prototype.setButtonStates=function(){var b=a("#sortModal"),c=b.find(".multi-sort-name:first option").length,d=b.find("tbody tr").length;d==c&&b.find("#add").attr("disabled","disabled"),d>1&&b.find("#delete").removeAttr("disabled"),c>d&&b.find("#add").removeAttr("disabled"),1==d&&b.find("#delete").attr("disabled","disabled")}}(jQuery),!function(a){"use strict";var b=function(b,c){var d=-1;return a.each(b,function(a,b){return b.field===c?(d=a,!1):!0}),d};a.extend(a.fn.bootstrapTable.defaults,{reorderableColumns:!1,maxMovingRows:10,onReorderColumn:function(){return!1}}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"reorder-column.bs.table":"onReorderColumn"});var c=a.fn.bootstrapTable.Constructor,d=c.prototype.initHeader,e=c.prototype.toggleColumn,f=c.prototype.toggleView,g=c.prototype.resetView;c.prototype.initHeader=function(){d.apply(this,Array.prototype.slice.apply(arguments)),this.options.reorderableColumns&&this.makeRowsReorderable()},c.prototype.toggleColumn=function(){e.apply(this,Array.prototype.slice.apply(arguments)),this.options.reorderableColumns&&this.makeRowsReorderable()},c.prototype.toggleView=function(){f.apply(this,Array.prototype.slice.apply(arguments)),this.options.reorderableColumns&&(this.options.cardView||this.makeRowsReorderable())},c.prototype.resetView=function(){g.apply(this,Array.prototype.slice.apply(arguments)),this.options.reorderableColumns&&this.makeRowsReorderable()},c.prototype.makeRowsReorderable=function(){var c=this;try{a(this.$el).dragtable("destroy")}catch(d){}a(this.$el).dragtable({maxMovingRows:c.options.maxMovingRows,clickDelay:200,beforeStop:function(){var d=[],e=[],f=-1;c.$header.find("th").each(function(){d.push(a(this).data("field"))});for(var g=0;g<d.length;g++)f=b(c.options.columns,d[g]),-1!==f&&(e.push(c.options.columns[f]),c.options.columns.splice(f,1));c.options.columns=c.options.columns.concat(e),c.header.fields=d,c.resetView(),c.trigger("reorder-column",d)}})}}(jQuery),!function(a){"use strict";var b=function(a,b){return{id:"customId_"+b}};a.extend(a.fn.bootstrapTable.defaults,{reorderableRows:!1,onDragStyle:null,onDropStyle:null,onDragClass:"reorder_rows_onDragClass",dragHandle:null,useRowAttrFunc:!1,onReorderRowsDrag:function(){return!1},onReorderRowsDrop:function(){return!1},onReorderRow:function(){return!1}}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"reorder-row.bs.table":"onReorderRow"});var c=a.fn.bootstrapTable.Constructor,d=c.prototype.init,e=c.prototype.initSearch;c.prototype.init=function(){if(d.apply(this,Array.prototype.slice.apply(arguments)),this.options.reorderableRows){var a=this;this.options.useRowAttrFunc&&(this.options.rowAttributes=b);var c=this.options.onPostBody;this.options.onPostBody=function(){setTimeout(function(){a.makeRowsReorderable(),c.apply()},1)}}},c.prototype.initSearch=function(){e.apply(this,Array.prototype.slice.apply(arguments)),!this.options.reorderableRows},c.prototype.makeRowsReorderable=function(){if(!this.options.cardView){var a=this;this.$el.tableDnD({onDragStyle:a.options.onDragStyle,onDropStyle:a.options.onDropStyle,onDragClass:a.options.onDragClass,onDrop:a.onDrop,onDragStart:a.options.onReorderRowsDrag,dragHandle:a.options.dragHandle})}},c.prototype.onDrop=function(b,c){for(var d=a(b),e=d.data("bootstrap.table"),f=d.data("bootstrap.table").options,c=null,g=[],h=0;h<b.tBodies[0].rows.length;h++)c=a(b.tBodies[0].rows[h]),g.push(f.data[c.data("index")]),c.data("index",h).attr("data-index",h);f.data=g,f.onReorderRowsDrop.apply(b,c),e.trigger("reorder-row",g)}}(jQuery),function(a){"use strict";var b=function(a){a.$el.colResizable({disable:!0}),a.$el.colResizable({liveDrag:a.options.liveDrag,fixed:a.options.fixed,headerOnly:a.options.headerOnly,minWidth:a.options.minWidth,hoverCursor:a.options.hoverCursor,dragCursor:a.options.dragCursor,onResize:a.onResize,onDrag:a.options.onResizableDrag})};a.extend(a.fn.bootstrapTable.defaults,{resizable:!1,liveDrag:!1,fixed:!0,headerOnly:!1,minWidth:15,hoverCursor:"e-resize",dragCursor:"e-resize",onResizableResize:function(){return!1},onResizableDrag:function(){return!1}});var c=a.fn.bootstrapTable.Constructor,d=c.prototype.toggleView,e=c.prototype.resetView;c.prototype.toggleView=function(){d.apply(this,Array.prototype.slice.apply(arguments)),this.options.resizable&&this.options.cardView&&a(this.$el).colResizable({disable:!0})},c.prototype.resetView=function(){var a=this;e.apply(this,Array.prototype.slice.apply(arguments)),this.options.resizable&&setTimeout(function(){b(a)},100)},c.prototype.onResize=function(b){var c=a(b.currentTarget);c.bootstrapTable("resetView"),c.data("bootstrap.table").options.onResizableResize.apply(b)}}(jQuery),!function(a){"use strict";var b=!1,c=function(a){var b=arguments,c=!0,d=1;return a=a.replace(/%s/g,function(){var a=b[d++];return"undefined"==typeof a?(c=!1,""):a}),c?a:""},d=function(b,c,d,e){if("string"==typeof c){var f=c.split(".");f.length>1?(c=window,a.each(f,function(a,b){c=c[b]})):c=window[c]}return"object"==typeof c?c:"function"==typeof c?c.apply(b,d):e},e=function(b,d,e,g){if(a("#avdSearchModal").hasClass("modal"))a("#avdSearchModal").modal();else{var h='<div id="avdSearchModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="mySmallModalLabel" aria-hidden="true">';h+='<div class="modal-dialog modal-xs">',h+=' <div class="modal-content">',h+='  <div class="modal-header">',h+='   <button type="button" class="close" data-dismiss="modal" aria-hidden="true" >&times;</button>',h+=c('   <h4 class="modal-title">%s</h4>',d),h+="  </div>",h+='  <div class="modal-body modal-body-custom">',h+='   <div class="container-fluid" id="avdSearchModalContent" style="padding-right: 0px;padding-left: 0px;" >',h+="   </div>",h+="  </div>",h+="  </div>",h+=" </div>",h+="</div>",a("body").append(a(h));var i=f(b,e,g),j=0;a("#avdSearchModalContent").append(i.join("")),a("#"+g.options.idForm).off("keyup blur","input").on("keyup blur","input",function(a){clearTimeout(j),j=setTimeout(function(){g.onColumnAdvancedSearch(a)},g.options.searchTimeOut)}),a("#btnCloseAvd").click(function(){a("#avdSearchModal").modal("hide")}),a("#avdSearchModal").modal()}},f=function(a,b,d){var e=[];e.push(c('<form class="form-horizontal" id="%s" action="%s" >',d.options.idForm,d.options.actionForm));for(var f in a){var g=a[f];!g.checkbox&&g.visible&&g.searchable&&(e.push('<div class="form-group">'),e.push(c('<label class="col-sm-4 control-label">%s</label>',g.title)),e.push('<div class="col-sm-6">'),e.push(c('<input type="text" class="form-control input-md" name="%s" placeholder="%s" id="%s">',g.field,g.title,g.field)),e.push("</div>"),e.push("</div>"))}return e.push('<div class="form-group">'),e.push('<div class="col-sm-offset-9 col-sm-3">'),e.push(c('<button type="button" id="btnCloseAvd" class="btn btn-default" >%s</button>',b)),e.push("</div>"),e.push("</div>"),e.push("</form>"),e};a.extend(a.fn.bootstrapTable.defaults,{advancedSearch:!1,idForm:"advancedSearch",actionForm:"",idTable:void 0,onColumnAdvancedSearch:function(){return!1}}),a.extend(a.fn.bootstrapTable.defaults.icons,{advancedSearchIcon:"glyphicon-chevron-down"}),a.extend(a.fn.bootstrapTable.Constructor.EVENTS,{"column-advanced-search.bs.table":"onColumnAdvancedSearch"}),a.extend(a.fn.bootstrapTable.locales,{formatAdvancedSearch:function(){return"Advanced search"},formatAdvancedCloseButton:function(){return"Close"}}),a.extend(a.fn.bootstrapTable.defaults,a.fn.bootstrapTable.locales);var g=a.fn.bootstrapTable.Constructor,h=g.prototype.initToolbar,i=g.prototype.load,j=g.prototype.initSearch;g.prototype.initToolbar=function(){if(h.apply(this,Array.prototype.slice.apply(arguments)),this.options.search&&this.options.advancedSearch){var a=this,b=[];b.push(c('<div class="columns columns-%s btn-group pull-%s" role="group">',this.options.buttonsAlign,this.options.buttonsAlign)),b.push(c('<button class="btn btn-default%s" type="button" name="advancedSearch" title="%s">',void 0===a.options.iconSize?"":" btn-"+a.options.iconSize,a.options.formatAdvancedSearch())),b.push(c('<i class="%s %s"></i>',a.options.iconsPrefix,a.options.icons.advancedSearchIcon)),b.push("</button></div>"),a.$toolbar.prepend(b.join("")),a.$toolbar.find('button[name="advancedSearch"]').off("click").on("click",function(){e(a.options.columns,a.options.formatAdvancedSearch(),a.options.formatAdvancedCloseButton(),a)})}},g.prototype.load=function(){if(i.apply(this,Array.prototype.slice.apply(arguments)),"undefined"!=typeof this.options.idTable&&!b){var c=parseInt(a(".bootstrap-table").height());c+=10,a("#"+this.options.idTable).bootstrapTable("resetView",{height:c}),b=!0}},g.prototype.initSearch=function(){j.apply(this,Array.prototype.slice.apply(arguments));var b=this,c=a.isEmptyObject(this.filterColumnsPartial)?null:this.filterColumnsPartial;this.data=c?a.grep(this.data,function(e,f){for(var g in c){var h=c[g].toLowerCase(),i=e[g];if(i=d(b.header,b.header.formatters[a.inArray(g,b.header.fields)],[i,e,f],i),-1===a.inArray(g,b.header.fields)||"string"!=typeof i&&"number"!=typeof i||-1===(i+"").toLowerCase().indexOf(h))return!1}return!0}):this.data},g.prototype.onColumnAdvancedSearch=function(b){var c=a.trim(a(b.currentTarget).val()),d=a(b.currentTarget)[0].id;a.isEmptyObject(this.filterColumnsPartial)&&(this.filterColumnsPartial={}),c?this.filterColumnsPartial[d]=c:delete this.filterColumnsPartial[d],this.options.pageNumber=1,this.onSearch(b),this.updatePagination(),this.trigger("column-advanced-search",d,c)}}(jQuery);

////https://github.com/aterrien/jQuery-Knob
(function(e){if(typeof define==="function"&&define.amd){define(["jquery"],e)}else{e(jQuery)}})(function(e){"use strict";var t={},n=Math.max,r=Math.min;t.c={};t.c.d=e(document);t.c.t=function(e){return e.originalEvent.touches.length-1};t.o=function(){var n=this;this.o=null;this.$=null;this.i=null;this.g=null;this.v=null;this.cv=null;this.x=0;this.y=0;this.w=0;this.h=0;this.$c=null;this.c=null;this.t=0;this.isInit=false;this.fgColor=null;this.pColor=null;this.dH=null;this.cH=null;this.eH=null;this.rH=null;this.scale=1;this.relative=false;this.relativeWidth=false;this.relativeHeight=false;this.$div=null;this.run=function(){var t=function(e,t){var r;for(r in t){n.o[r]=t[r]}n._carve().init();n._configure()._draw()};if(this.$.data("kontroled"))return;this.$.data("kontroled",true);this.extend();this.o=e.extend({min:this.$.data("min")!==undefined?this.$.data("min"):0,max:this.$.data("max")!==undefined?this.$.data("max"):100,stopper:true,readOnly:this.$.data("readonly")||this.$.attr("readonly")==="readonly",cursor:this.$.data("cursor")===true&&30||this.$.data("cursor")||0,thickness:this.$.data("thickness")&&Math.max(Math.min(this.$.data("thickness"),1),.01)||.35,lineCap:this.$.data("linecap")||"butt",width:this.$.data("width")||200,height:this.$.data("height")||200,displayInput:this.$.data("displayinput")==null||this.$.data("displayinput"),displayPrevious:this.$.data("displayprevious"),fgColor:this.$.data("fgcolor")||"#87CEEB",inputColor:this.$.data("inputcolor"),font:this.$.data("font")||"Arial",fontWeight:this.$.data("font-weight")||"bold",inline:false,step:this.$.data("step")||1,rotation:this.$.data("rotation"),draw:null,change:null,cancel:null,release:null,format:function(e){return e},parse:function(e){return parseFloat(e)}},this.o);this.o.flip=this.o.rotation==="anticlockwise"||this.o.rotation==="acw";if(!this.o.inputColor){this.o.inputColor=this.o.fgColor}if(this.$.is("fieldset")){this.v={};this.i=this.$.find("input");this.i.each(function(t){var r=e(this);n.i[t]=r;n.v[t]=n.o.parse(r.val());r.bind("change blur",function(){var e={};e[t]=r.val();n.val(n._validate(e))})});this.$.find("legend").remove()}else{this.i=this.$;this.v=this.o.parse(this.$.val());this.v===""&&(this.v=this.o.min);this.$.bind("change blur",function(){n.val(n._validate(n.o.parse(n.$.val())))})}!this.o.displayInput&&this.$.hide();this.$c=e(document.createElement("canvas")).attr({width:this.o.width,height:this.o.height});this.$div=e('<div style="position:relative;'+"width:"+this.o.width+"px;height:"+this.o.height+"px;"+'"></div>');this.$.wrap(this.$div).before(this.$c);this.$div=this.$.parent();if(typeof G_vmlCanvasManager!=="undefined"){G_vmlCanvasManager.initElement(this.$c[0])}this.c=this.$c[0].getContext?this.$c[0].getContext("2d"):null;if(!this.c){throw{name:"CanvasNotSupportedException",message:"Canvas not supported. Please use excanvas on IE8.0.",toString:function(){return this.name+": "+this.message}}}this.scale=(window.devicePixelRatio||1)/(this.c.webkitBackingStorePixelRatio||this.c.mozBackingStorePixelRatio||this.c.msBackingStorePixelRatio||this.c.oBackingStorePixelRatio||this.c.backingStorePixelRatio||1);this.relativeWidth=this.o.width%1!==0&&this.o.width.indexOf("%");this.relativeHeight=this.o.height%1!==0&&this.o.height.indexOf("%");this.relative=this.relativeWidth||this.relativeHeight;this._carve();if(this.v instanceof Object){this.cv={};this.copy(this.v,this.cv)}else{this.cv=this.v}this.$.bind("configure",t).parent().bind("configure",t);this._listen()._configure()._xy().init();this.isInit=true;this.$.val(this.o.format(this.v));this._draw();return this};this._carve=function(){if(this.relative){var e=this.relativeWidth?this.$div.parent().width()*parseInt(this.o.width)/100:this.$div.parent().width(),t=this.relativeHeight?this.$div.parent().height()*parseInt(this.o.height)/100:this.$div.parent().height();this.w=this.h=Math.min(e,t)}else{this.w=this.o.width;this.h=this.o.height}this.$div.css({width:this.w+"px",height:this.h+"px"});this.$c.attr({width:this.w,height:this.h});if(this.scale!==1){this.$c[0].width=this.$c[0].width*this.scale;this.$c[0].height=this.$c[0].height*this.scale;this.$c.width(this.w);this.$c.height(this.h)}return this};this._draw=function(){var e=true;n.g=n.c;n.clear();n.dH&&(e=n.dH());e!==false&&n.draw()};this._touch=function(e){var r=function(e){var t=n.xy2val(e.originalEvent.touches[n.t].pageX,e.originalEvent.touches[n.t].pageY);if(t==n.cv)return;if(n.cH&&n.cH(t)===false)return;n.change(n._validate(t));n._draw()};this.t=t.c.t(e);r(e);t.c.d.bind("touchmove.k",r).bind("touchend.k",function(){t.c.d.unbind("touchmove.k touchend.k");n.val(n.cv)});return this};this._mouse=function(e){var r=function(e){var t=n.xy2val(e.pageX,e.pageY);if(t==n.cv)return;if(n.cH&&n.cH(t)===false)return;n.change(n._validate(t));n._draw()};r(e);t.c.d.bind("mousemove.k",r).bind("keyup.k",function(e){if(e.keyCode===27){t.c.d.unbind("mouseup.k mousemove.k keyup.k");if(n.eH&&n.eH()===false)return;n.cancel()}}).bind("mouseup.k",function(e){t.c.d.unbind("mousemove.k mouseup.k keyup.k");n.val(n.cv)});return this};this._xy=function(){var e=this.$c.offset();this.x=e.left;this.y=e.top;return this};this._listen=function(){if(!this.o.readOnly){this.$c.bind("mousedown",function(e){e.preventDefault();n._xy()._mouse(e)}).bind("touchstart",function(e){e.preventDefault();n._xy()._touch(e)});this.listen()}else{this.$.attr("readonly","readonly")}if(this.relative){e(window).resize(function(){n._carve().init();n._draw()})}return this};this._configure=function(){if(this.o.draw)this.dH=this.o.draw;if(this.o.change)this.cH=this.o.change;if(this.o.cancel)this.eH=this.o.cancel;if(this.o.release)this.rH=this.o.release;if(this.o.displayPrevious){this.pColor=this.h2rgba(this.o.fgColor,"0.4");this.fgColor=this.h2rgba(this.o.fgColor,"0.6")}else{this.fgColor=this.o.fgColor}return this};this._clear=function(){this.$c[0].width=this.$c[0].width};this._validate=function(e){var t=~~((e<0?-.5:.5)+e/this.o.step)*this.o.step;return Math.round(t*100)/100};this.listen=function(){};this.extend=function(){};this.init=function(){};this.change=function(e){};this.val=function(e){};this.xy2val=function(e,t){};this.draw=function(){};this.clear=function(){this._clear()};this.h2rgba=function(e,t){var n;e=e.substring(1,7);n=[parseInt(e.substring(0,2),16),parseInt(e.substring(2,4),16),parseInt(e.substring(4,6),16)];return"rgba("+n[0]+","+n[1]+","+n[2]+","+t+")"};this.copy=function(e,t){for(var n in e){t[n]=e[n]}}};t.Dial=function(){t.o.call(this);this.startAngle=null;this.xy=null;this.radius=null;this.lineWidth=null;this.cursorExt=null;this.w2=null;this.PI2=2*Math.PI;this.extend=function(){this.o=e.extend({bgColor:this.$.data("bgcolor")||"#EEEEEE",angleOffset:this.$.data("angleoffset")||0,angleArc:this.$.data("anglearc")||360,inline:true},this.o)};this.val=function(e,t){if(null!=e){e=this.o.parse(e);if(t!==false&&e!=this.v&&this.rH&&this.rH(e)===false){return}this.cv=this.o.stopper?n(r(e,this.o.max),this.o.min):e;this.v=this.cv;this.$.val(this.o.format(this.v));this._draw()}else{return this.v}};this.xy2val=function(e,t){var i,s;i=Math.atan2(e-(this.x+this.w2),-(t-this.y-this.w2))-this.angleOffset;if(this.o.flip){i=this.angleArc-i-this.PI2}if(this.angleArc!=this.PI2&&i<0&&i>-.5){i=0}else if(i<0){i+=this.PI2}s=i*(this.o.max-this.o.min)/this.angleArc+this.o.min;this.o.stopper&&(s=n(r(s,this.o.max),this.o.min));return s};this.listen=function(){var t=this,i,s,o=function(e){e.preventDefault();var o=e.originalEvent,u=o.detail||o.wheelDeltaX,a=o.detail||o.wheelDeltaY,f=t._validate(t.o.parse(t.$.val()))+(u>0||a>0?t.o.step:u<0||a<0?-t.o.step:0);f=n(r(f,t.o.max),t.o.min);t.val(f,false);if(t.rH){clearTimeout(i);i=setTimeout(function(){t.rH(f);i=null},100);if(!s){s=setTimeout(function(){if(i)t.rH(f);s=null},200)}}},u,a,f=1,l={37:-t.o.step,38:t.o.step,39:t.o.step,40:-t.o.step};this.$.bind("keydown",function(i){var s=i.keyCode;if(s>=96&&s<=105){s=i.keyCode=s-48}u=parseInt(String.fromCharCode(s));if(isNaN(u)){s!==13&&s!==8&&s!==9&&s!==189&&(s!==190||t.$.val().match(/\./))&&i.preventDefault();if(e.inArray(s,[37,38,39,40])>-1){i.preventDefault();var o=t.o.parse(t.$.val())+l[s]*f;t.o.stopper&&(o=n(r(o,t.o.max),t.o.min));t.change(t._validate(o));t._draw();a=window.setTimeout(function(){f*=2},30)}}}).bind("keyup",function(e){if(isNaN(u)){if(a){window.clearTimeout(a);a=null;f=1;t.val(t.$.val())}}else{t.$.val()>t.o.max&&t.$.val(t.o.max)||t.$.val()<t.o.min&&t.$.val(t.o.min)}});this.$c.bind("mousewheel DOMMouseScroll",o);this.$.bind("mousewheel DOMMouseScroll",o)};this.init=function(){if(this.v<this.o.min||this.v>this.o.max){this.v=this.o.min}this.$.val(this.v);this.w2=this.w/2;this.cursorExt=this.o.cursor/100;this.xy=this.w2*this.scale;this.lineWidth=this.xy*this.o.thickness;this.lineCap=this.o.lineCap;this.radius=this.xy-this.lineWidth/2;this.o.angleOffset&&(this.o.angleOffset=isNaN(this.o.angleOffset)?0:this.o.angleOffset);this.o.angleArc&&(this.o.angleArc=isNaN(this.o.angleArc)?this.PI2:this.o.angleArc);this.angleOffset=this.o.angleOffset*Math.PI/180;this.angleArc=this.o.angleArc*Math.PI/180;this.startAngle=1.5*Math.PI+this.angleOffset;this.endAngle=1.5*Math.PI+this.angleOffset+this.angleArc;var e=n(String(Math.abs(this.o.max)).length,String(Math.abs(this.o.min)).length,2)+2;this.o.displayInput&&this.i.css({width:(this.w/2+4>>0)+"px",height:(this.w/3>>0)+"px",position:"absolute","vertical-align":"middle","margin-top":(this.w/3>>0)+"px","margin-left":"-"+(this.w*3/4+2>>0)+"px",border:0,background:"none",font:this.o.fontWeight+" "+(this.w/e>>0)+"px "+this.o.font,"text-align":"center",color:this.o.inputColor||this.o.fgColor,padding:"0px","-webkit-appearance":"none"})||this.i.css({width:"0px",visibility:"hidden"})};this.change=function(e){this.cv=e;this.$.val(this.o.format(e))};this.angle=function(e){return(e-this.o.min)*this.angleArc/(this.o.max-this.o.min)};this.arc=function(e){var t,n;e=this.angle(e);if(this.o.flip){t=this.endAngle+1e-5;n=t-e-1e-5}else{t=this.startAngle-1e-5;n=t+e+1e-5}this.o.cursor&&(t=n-this.cursorExt)&&(n=n+this.cursorExt);return{s:t,e:n,d:this.o.flip&&!this.o.cursor}};this.draw=function(){var e=this.g,t=this.arc(this.cv),n,r=1;e.lineWidth=this.lineWidth;e.lineCap=this.lineCap;if(this.o.bgColor!=="none"){e.beginPath();e.strokeStyle=this.o.bgColor;e.arc(this.xy,this.xy,this.radius,this.endAngle-1e-5,this.startAngle+1e-5,true);e.stroke()}if(this.o.displayPrevious){n=this.arc(this.v);e.beginPath();e.strokeStyle=this.pColor;e.arc(this.xy,this.xy,this.radius,n.s,n.e,n.d);e.stroke();r=this.cv==this.v}e.beginPath();e.strokeStyle=r?this.o.fgColor:this.fgColor;e.arc(this.xy,this.xy,this.radius,t.s,t.e,t.d);e.stroke()};this.cancel=function(){this.val(this.v)}};e.fn.dial=e.fn.knob=function(n){return this.each(function(){var r=new t.Dial;r.o=n;r.$=e(this);r.run()}).parent()}})

/*! =======================================================
                      VERSION  4.9.1              
========================================================= */
/*! =========================================================
 * bootstrap-slider.js
 *
 * Maintainers:
 *		Kyle Kemp
 *			- Twitter: @seiyria
 *			- Github:  seiyria
 *		Rohit Kalkur
 *			- Twitter: @Rovolutionary
 *			- Github:  rovolution
 *
 * =========================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================= */
!function(a,b){if("function"==typeof define&&define.amd)define(["jquery"],b);else if("object"==typeof module&&module.exports){var c;try{c=require("jquery")}catch(d){c=null}module.exports=b(c)}else a.Slider=b(a.jQuery)}(this,function(a){var b;return function(a){"use strict";function b(){}function c(a){function c(b){b.prototype.option||(b.prototype.option=function(b){a.isPlainObject(b)&&(this.options=a.extend(!0,this.options,b))})}function e(b,c){a.fn[b]=function(e){if("string"==typeof e){for(var g=d.call(arguments,1),h=0,i=this.length;i>h;h++){var j=this[h],k=a.data(j,b);if(k)if(a.isFunction(k[e])&&"_"!==e.charAt(0)){var l=k[e].apply(k,g);if(void 0!==l&&l!==k)return l}else f("no such method '"+e+"' for "+b+" instance");else f("cannot call methods on "+b+" prior to initialization; attempted to call '"+e+"'")}return this}var m=this.map(function(){var d=a.data(this,b);return d?(d.option(e),d._init()):(d=new c(this,e),a.data(this,b,d)),a(this)});return!m||m.length>1?m:m[0]}}if(a){var f="undefined"==typeof console?b:function(a){console.error(a)};return a.bridget=function(a,b){c(b),e(a,b)},a.bridget}}var d=Array.prototype.slice;c(a)}(a),function(a){function c(b,c){function d(a,b){var c="data-slider-"+b.replace(/_/g,"-"),d=a.getAttribute(c);try{return JSON.parse(d)}catch(e){return d}}"string"==typeof b?this.element=document.querySelector(b):b instanceof HTMLElement&&(this.element=b),c=c?c:{};for(var f=Object.keys(this.defaultOptions),g=0;g<f.length;g++){var h=f[g],i=c[h];i="undefined"!=typeof i?i:d(this.element,h),i=null!==i?i:this.defaultOptions[h],this.options||(this.options={}),this.options[h]=i}var j,k,l,m,n,o=this.element.style.width,p=!1,q=this.element.parentNode;if(this.sliderElem)p=!0;else{this.sliderElem=document.createElement("div"),this.sliderElem.className="slider";var r=document.createElement("div");if(r.className="slider-track",k=document.createElement("div"),k.className="slider-track-low",j=document.createElement("div"),j.className="slider-selection",l=document.createElement("div"),l.className="slider-track-high",m=document.createElement("div"),m.className="slider-handle min-slider-handle",n=document.createElement("div"),n.className="slider-handle max-slider-handle",r.appendChild(k),r.appendChild(j),r.appendChild(l),this.ticks=[],Array.isArray(this.options.ticks)&&this.options.ticks.length>0){for(g=0;g<this.options.ticks.length;g++){var s=document.createElement("div");s.className="slider-tick",this.ticks.push(s),r.appendChild(s)}j.className+=" tick-slider-selection"}if(r.appendChild(m),r.appendChild(n),this.tickLabels=[],Array.isArray(this.options.ticks_labels)&&this.options.ticks_labels.length>0)for(this.tickLabelContainer=document.createElement("div"),this.tickLabelContainer.className="slider-tick-label-container",g=0;g<this.options.ticks_labels.length;g++){var t=document.createElement("div");t.className="slider-tick-label",t.innerHTML=this.options.ticks_labels[g],this.tickLabels.push(t),this.tickLabelContainer.appendChild(t)}var u=function(a){var b=document.createElement("div");b.className="tooltip-arrow";var c=document.createElement("div");c.className="tooltip-inner",a.appendChild(b),a.appendChild(c)},v=document.createElement("div");v.className="tooltip tooltip-main",u(v);var w=document.createElement("div");w.className="tooltip tooltip-min",u(w);var x=document.createElement("div");x.className="tooltip tooltip-max",u(x),this.sliderElem.appendChild(r),this.sliderElem.appendChild(v),this.sliderElem.appendChild(w),this.sliderElem.appendChild(x),this.tickLabelContainer&&this.sliderElem.appendChild(this.tickLabelContainer),q.insertBefore(this.sliderElem,this.element),this.element.style.display="none"}if(a&&(this.$element=a(this.element),this.$sliderElem=a(this.sliderElem)),this.eventToCallbackMap={},this.sliderElem.id=this.options.id,this.touchCapable="ontouchstart"in window||window.DocumentTouch&&document instanceof window.DocumentTouch,this.tooltip=this.sliderElem.querySelector(".tooltip-main"),this.tooltipInner=this.tooltip.querySelector(".tooltip-inner"),this.tooltip_min=this.sliderElem.querySelector(".tooltip-min"),this.tooltipInner_min=this.tooltip_min.querySelector(".tooltip-inner"),this.tooltip_max=this.sliderElem.querySelector(".tooltip-max"),this.tooltipInner_max=this.tooltip_max.querySelector(".tooltip-inner"),e[this.options.scale]&&(this.options.scale=e[this.options.scale]),p===!0&&(this._removeClass(this.sliderElem,"slider-horizontal"),this._removeClass(this.sliderElem,"slider-vertical"),this._removeClass(this.tooltip,"hide"),this._removeClass(this.tooltip_min,"hide"),this._removeClass(this.tooltip_max,"hide"),["left","top","width","height"].forEach(function(a){this._removeProperty(this.trackLow,a),this._removeProperty(this.trackSelection,a),this._removeProperty(this.trackHigh,a)},this),[this.handle1,this.handle2].forEach(function(a){this._removeProperty(a,"left"),this._removeProperty(a,"top")},this),[this.tooltip,this.tooltip_min,this.tooltip_max].forEach(function(a){this._removeProperty(a,"left"),this._removeProperty(a,"top"),this._removeProperty(a,"margin-left"),this._removeProperty(a,"margin-top"),this._removeClass(a,"right"),this._removeClass(a,"top")},this)),"vertical"===this.options.orientation?(this._addClass(this.sliderElem,"slider-vertical"),this.stylePos="top",this.mousePos="pageY",this.sizePos="offsetHeight",this._addClass(this.tooltip,"right"),this.tooltip.style.left="100%",this._addClass(this.tooltip_min,"right"),this.tooltip_min.style.left="100%",this._addClass(this.tooltip_max,"right"),this.tooltip_max.style.left="100%"):(this._addClass(this.sliderElem,"slider-horizontal"),this.sliderElem.style.width=o,this.options.orientation="horizontal",this.stylePos="left",this.mousePos="pageX",this.sizePos="offsetWidth",this._addClass(this.tooltip,"top"),this.tooltip.style.top=-this.tooltip.outerHeight-14+"px",this._addClass(this.tooltip_min,"top"),this.tooltip_min.style.top=-this.tooltip_min.outerHeight-14+"px",this._addClass(this.tooltip_max,"top"),this.tooltip_max.style.top=-this.tooltip_max.outerHeight-14+"px"),Array.isArray(this.options.ticks)&&this.options.ticks.length>0&&(this.options.max=Math.max.apply(Math,this.options.ticks),this.options.min=Math.min.apply(Math,this.options.ticks)),Array.isArray(this.options.value)?this.options.range=!0:this.options.range&&(this.options.value=[this.options.value,this.options.max]),this.trackLow=k||this.trackLow,this.trackSelection=j||this.trackSelection,this.trackHigh=l||this.trackHigh,"none"===this.options.selection&&(this._addClass(this.trackLow,"hide"),this._addClass(this.trackSelection,"hide"),this._addClass(this.trackHigh,"hide")),this.handle1=m||this.handle1,this.handle2=n||this.handle2,p===!0)for(this._removeClass(this.handle1,"round triangle"),this._removeClass(this.handle2,"round triangle hide"),g=0;g<this.ticks.length;g++)this._removeClass(this.ticks[g],"round triangle hide");var y=["round","triangle","custom"],z=-1!==y.indexOf(this.options.handle);if(z)for(this._addClass(this.handle1,this.options.handle),this._addClass(this.handle2,this.options.handle),g=0;g<this.ticks.length;g++)this._addClass(this.ticks[g],this.options.handle);this.offset=this._offset(this.sliderElem),this.size=this.sliderElem[this.sizePos],this.setValue(this.options.value),this.handle1Keydown=this._keydown.bind(this,0),this.handle1.addEventListener("keydown",this.handle1Keydown,!1),this.handle2Keydown=this._keydown.bind(this,1),this.handle2.addEventListener("keydown",this.handle2Keydown,!1),this.mousedown=this._mousedown.bind(this),this.touchCapable&&this.sliderElem.addEventListener("touchstart",this.mousedown,!1),this.sliderElem.addEventListener("mousedown",this.mousedown,!1),"hide"===this.options.tooltip?(this._addClass(this.tooltip,"hide"),this._addClass(this.tooltip_min,"hide"),this._addClass(this.tooltip_max,"hide")):"always"===this.options.tooltip?(this._showTooltip(),this._alwaysShowTooltip=!0):(this.showTooltip=this._showTooltip.bind(this),this.hideTooltip=this._hideTooltip.bind(this),this.sliderElem.addEventListener("mouseenter",this.showTooltip,!1),this.sliderElem.addEventListener("mouseleave",this.hideTooltip,!1),this.handle1.addEventListener("focus",this.showTooltip,!1),this.handle1.addEventListener("blur",this.hideTooltip,!1),this.handle2.addEventListener("focus",this.showTooltip,!1),this.handle2.addEventListener("blur",this.hideTooltip,!1)),this.options.enabled?this.enable():this.disable()}var d={formatInvalidInputErrorMsg:function(a){return"Invalid input value '"+a+"' passed in"},callingContextNotSliderInstance:"Calling context element does not have instance of Slider bound to it. Check your code to make sure the JQuery object returned from the call to the slider() initializer is calling the method"},e={linear:{toValue:function(a){var b=a/100*(this.options.max-this.options.min);if(this.options.ticks_positions.length>0){for(var c,d,e,f=0,g=0;g<this.options.ticks_positions.length;g++)if(a<=this.options.ticks_positions[g]){c=g>0?this.options.ticks[g-1]:0,e=g>0?this.options.ticks_positions[g-1]:0,d=this.options.ticks[g],f=this.options.ticks_positions[g];break}if(g>0){var h=(a-e)/(f-e);b=c+h*(d-c)}}var i=this.options.min+Math.round(b/this.options.step)*this.options.step;return i<this.options.min?this.options.min:i>this.options.max?this.options.max:i},toPercentage:function(a){if(this.options.max===this.options.min)return 0;if(this.options.ticks_positions.length>0){for(var b,c,d,e=0,f=0;f<this.options.ticks.length;f++)if(a<=this.options.ticks[f]){b=f>0?this.options.ticks[f-1]:0,d=f>0?this.options.ticks_positions[f-1]:0,c=this.options.ticks[f],e=this.options.ticks_positions[f];break}if(f>0){var g=(a-b)/(c-b);return d+g*(e-d)}}return 100*(a-this.options.min)/(this.options.max-this.options.min)}},logarithmic:{toValue:function(a){var b=0===this.options.min?0:Math.log(this.options.min),c=Math.log(this.options.max),d=Math.exp(b+(c-b)*a/100);return d=this.options.min+Math.round((d-this.options.min)/this.options.step)*this.options.step,d<this.options.min?this.options.min:d>this.options.max?this.options.max:d},toPercentage:function(a){if(this.options.max===this.options.min)return 0;var b=Math.log(this.options.max),c=0===this.options.min?0:Math.log(this.options.min),d=0===a?0:Math.log(a);return 100*(d-c)/(b-c)}}};if(b=function(a,b){return c.call(this,a,b),this},b.prototype={_init:function(){},constructor:b,defaultOptions:{id:"",min:0,max:10,step:1,precision:0,orientation:"horizontal",value:5,range:!1,selection:"before",tooltip:"show",tooltip_split:!1,handle:"round",reversed:!1,enabled:!0,formatter:function(a){return Array.isArray(a)?a[0]+" : "+a[1]:a},natural_arrow_keys:!1,ticks:[],ticks_positions:[],ticks_labels:[],ticks_snap_bounds:0,scale:"linear",focus:!1},over:!1,inDrag:!1,getValue:function(){return this.options.range?this.options.value:this.options.value[0]},setValue:function(a,b,c){a||(a=0);var d=this.getValue();this.options.value=this._validateInputValue(a);var e=this._applyPrecision.bind(this);this.options.range?(this.options.value[0]=e(this.options.value[0]),this.options.value[1]=e(this.options.value[1]),this.options.value[0]=Math.max(this.options.min,Math.min(this.options.max,this.options.value[0])),this.options.value[1]=Math.max(this.options.min,Math.min(this.options.max,this.options.value[1]))):(this.options.value=e(this.options.value),this.options.value=[Math.max(this.options.min,Math.min(this.options.max,this.options.value))],this._addClass(this.handle2,"hide"),this.options.value[1]="after"===this.options.selection?this.options.max:this.options.min),this.percentage=this.options.max>this.options.min?[this._toPercentage(this.options.value[0]),this._toPercentage(this.options.value[1]),100*this.options.step/(this.options.max-this.options.min)]:[0,0,100],this._layout();var f=this.options.range?this.options.value:this.options.value[0];return b===!0&&this._trigger("slide",f),d!==f&&c===!0&&this._trigger("change",{oldValue:d,newValue:f}),this._setDataVal(f),this},destroy:function(){this._removeSliderEventHandlers(),this.sliderElem.parentNode.removeChild(this.sliderElem),this.element.style.display="",this._cleanUpEventCallbacksMap(),this.element.removeAttribute("data"),a&&(this._unbindJQueryEventHandlers(),this.$element.removeData("slider"))},disable:function(){return this.options.enabled=!1,this.handle1.removeAttribute("tabindex"),this.handle2.removeAttribute("tabindex"),this._addClass(this.sliderElem,"slider-disabled"),this._trigger("slideDisabled"),this},enable:function(){return this.options.enabled=!0,this.handle1.setAttribute("tabindex",0),this.handle2.setAttribute("tabindex",0),this._removeClass(this.sliderElem,"slider-disabled"),this._trigger("slideEnabled"),this},toggle:function(){return this.options.enabled?this.disable():this.enable(),this},isEnabled:function(){return this.options.enabled},on:function(a,b){return this._bindNonQueryEventHandler(a,b),this},getAttribute:function(a){return a?this.options[a]:this.options},setAttribute:function(a,b){return this.options[a]=b,this},refresh:function(){return this._removeSliderEventHandlers(),c.call(this,this.element,this.options),a&&a.data(this.element,"slider",this),this},relayout:function(){return this._layout(),this},_removeSliderEventHandlers:function(){this.handle1.removeEventListener("keydown",this.handle1Keydown,!1),this.handle1.removeEventListener("focus",this.showTooltip,!1),this.handle1.removeEventListener("blur",this.hideTooltip,!1),this.handle2.removeEventListener("keydown",this.handle2Keydown,!1),this.handle2.removeEventListener("focus",this.handle2Keydown,!1),this.handle2.removeEventListener("blur",this.handle2Keydown,!1),this.sliderElem.removeEventListener("mouseenter",this.showTooltip,!1),this.sliderElem.removeEventListener("mouseleave",this.hideTooltip,!1),this.sliderElem.removeEventListener("touchstart",this.mousedown,!1),this.sliderElem.removeEventListener("mousedown",this.mousedown,!1)},_bindNonQueryEventHandler:function(a,b){void 0===this.eventToCallbackMap[a]&&(this.eventToCallbackMap[a]=[]),this.eventToCallbackMap[a].push(b)},_cleanUpEventCallbacksMap:function(){for(var a=Object.keys(this.eventToCallbackMap),b=0;b<a.length;b++){var c=a[b];this.eventToCallbackMap[c]=null}},_showTooltip:function(){this.options.tooltip_split===!1?(this._addClass(this.tooltip,"in"),this.tooltip_min.style.display="none",this.tooltip_max.style.display="none"):(this._addClass(this.tooltip_min,"in"),this._addClass(this.tooltip_max,"in"),this.tooltip.style.display="none"),this.over=!0},_hideTooltip:function(){this.inDrag===!1&&this.alwaysShowTooltip!==!0&&(this._removeClass(this.tooltip,"in"),this._removeClass(this.tooltip_min,"in"),this._removeClass(this.tooltip_max,"in")),this.over=!1},_layout:function(){var a;if(a=this.options.reversed?[100-this.percentage[0],this.percentage[1]]:[this.percentage[0],this.percentage[1]],this.handle1.style[this.stylePos]=a[0]+"%",this.handle2.style[this.stylePos]=a[1]+"%",Array.isArray(this.options.ticks)&&this.options.ticks.length>0){var b=Math.max.apply(Math,this.options.ticks),c=Math.min.apply(Math,this.options.ticks),d="vertical"===this.options.orientation?"height":"width",e="vertical"===this.options.orientation?"marginTop":"marginLeft",f=this.size/(this.options.ticks.length-1);if(this.tickLabelContainer){var g=0;if(0===this.options.ticks_positions.length)this.tickLabelContainer.style[e]=-f/2+"px",g=this.tickLabelContainer.offsetHeight;else for(h=0;h<this.tickLabelContainer.childNodes.length;h++)this.tickLabelContainer.childNodes[h].offsetHeight>g&&(g=this.tickLabelContainer.childNodes[h].offsetHeight);"horizontal"===this.options.orientation&&(this.sliderElem.style.marginBottom=g+"px")}for(var h=0;h<this.options.ticks.length;h++){var i=this.options.ticks_positions[h]||100*(this.options.ticks[h]-c)/(b-c);this.ticks[h].style[this.stylePos]=i+"%",this._removeClass(this.ticks[h],"in-selection"),this.options.range?i>=a[0]&&i<=a[1]&&this._addClass(this.ticks[h],"in-selection"):"after"===this.options.selection&&i>=a[0]?this._addClass(this.ticks[h],"in-selection"):"before"===this.options.selection&&i<=a[0]&&this._addClass(this.ticks[h],"in-selection"),this.tickLabels[h]&&(this.tickLabels[h].style[d]=f+"px",void 0!==this.options.ticks_positions[h]&&(this.tickLabels[h].style.position="absolute",this.tickLabels[h].style[this.stylePos]=this.options.ticks_positions[h]+"%",this.tickLabels[h].style[e]=-f/2+"px"))}}if("vertical"===this.options.orientation)this.trackLow.style.top="0",this.trackLow.style.height=Math.min(a[0],a[1])+"%",this.trackSelection.style.top=Math.min(a[0],a[1])+"%",this.trackSelection.style.height=Math.abs(a[0]-a[1])+"%",this.trackHigh.style.bottom="0",this.trackHigh.style.height=100-Math.min(a[0],a[1])-Math.abs(a[0]-a[1])+"%";else{this.trackLow.style.left="0",this.trackLow.style.width=Math.min(a[0],a[1])+"%",this.trackSelection.style.left=Math.min(a[0],a[1])+"%",this.trackSelection.style.width=Math.abs(a[0]-a[1])+"%",this.trackHigh.style.right="0",this.trackHigh.style.width=100-Math.min(a[0],a[1])-Math.abs(a[0]-a[1])+"%";var j=this.tooltip_min.getBoundingClientRect(),k=this.tooltip_max.getBoundingClientRect();j.right>k.left?(this._removeClass(this.tooltip_max,"top"),this._addClass(this.tooltip_max,"bottom"),this.tooltip_max.style.top="18px"):(this._removeClass(this.tooltip_max,"bottom"),this._addClass(this.tooltip_max,"top"),this.tooltip_max.style.top=this.tooltip_min.style.top)}var l;if(this.options.range){l=this.options.formatter(this.options.value),this._setText(this.tooltipInner,l),this.tooltip.style[this.stylePos]=(a[1]+a[0])/2+"%","vertical"===this.options.orientation?this._css(this.tooltip,"margin-top",-this.tooltip.offsetHeight/2+"px"):this._css(this.tooltip,"margin-left",-this.tooltip.offsetWidth/2+"px"),"vertical"===this.options.orientation?this._css(this.tooltip,"margin-top",-this.tooltip.offsetHeight/2+"px"):this._css(this.tooltip,"margin-left",-this.tooltip.offsetWidth/2+"px");var m=this.options.formatter(this.options.value[0]);this._setText(this.tooltipInner_min,m);var n=this.options.formatter(this.options.value[1]);this._setText(this.tooltipInner_max,n),this.tooltip_min.style[this.stylePos]=a[0]+"%","vertical"===this.options.orientation?this._css(this.tooltip_min,"margin-top",-this.tooltip_min.offsetHeight/2+"px"):this._css(this.tooltip_min,"margin-left",-this.tooltip_min.offsetWidth/2+"px"),this.tooltip_max.style[this.stylePos]=a[1]+"%","vertical"===this.options.orientation?this._css(this.tooltip_max,"margin-top",-this.tooltip_max.offsetHeight/2+"px"):this._css(this.tooltip_max,"margin-left",-this.tooltip_max.offsetWidth/2+"px")}else l=this.options.formatter(this.options.value[0]),this._setText(this.tooltipInner,l),this.tooltip.style[this.stylePos]=a[0]+"%","vertical"===this.options.orientation?this._css(this.tooltip,"margin-top",-this.tooltip.offsetHeight/2+"px"):this._css(this.tooltip,"margin-left",-this.tooltip.offsetWidth/2+"px")},_removeProperty:function(a,b){a.style.removeProperty?a.style.removeProperty(b):a.style.removeAttribute(b)},_mousedown:function(a){if(!this.options.enabled)return!1;this.offset=this._offset(this.sliderElem),this.size=this.sliderElem[this.sizePos];var b=this._getPercentage(a);if(this.options.range){var c=Math.abs(this.percentage[0]-b),d=Math.abs(this.percentage[1]-b);this.dragged=d>c?0:1}else this.dragged=0;this.percentage[this.dragged]=this.options.reversed?100-b:b,this._layout(),this.touchCapable&&(document.removeEventListener("touchmove",this.mousemove,!1),document.removeEventListener("touchend",this.mouseup,!1)),this.mousemove&&document.removeEventListener("mousemove",this.mousemove,!1),this.mouseup&&document.removeEventListener("mouseup",this.mouseup,!1),this.mousemove=this._mousemove.bind(this),this.mouseup=this._mouseup.bind(this),this.touchCapable&&(document.addEventListener("touchmove",this.mousemove,!1),document.addEventListener("touchend",this.mouseup,!1)),document.addEventListener("mousemove",this.mousemove,!1),document.addEventListener("mouseup",this.mouseup,!1),this.inDrag=!0;var e=this._calculateValue();return this._trigger("slideStart",e),this._setDataVal(e),this.setValue(e,!1,!0),this._pauseEvent(a),this.options.focus&&this._triggerFocusOnHandle(this.dragged),!0},_triggerFocusOnHandle:function(a){0===a&&this.handle1.focus(),1===a&&this.handle2.focus()},_keydown:function(a,b){if(!this.options.enabled)return!1;var c;switch(b.keyCode){case 37:case 40:c=-1;break;case 39:case 38:c=1}if(c){if(this.options.natural_arrow_keys){var d="vertical"===this.options.orientation&&!this.options.reversed,e="horizontal"===this.options.orientation&&this.options.reversed;(d||e)&&(c=-c)}var f=this.options.value[a]+c*this.options.step;return this.options.range&&(f=[a?this.options.value[0]:f,a?f:this.options.value[1]]),this._trigger("slideStart",f),this._setDataVal(f),this.setValue(f,!0,!0),this._trigger("slideStop",f),this._setDataVal(f),this._layout(),this._pauseEvent(b),!1}},_pauseEvent:function(a){a.stopPropagation&&a.stopPropagation(),a.preventDefault&&a.preventDefault(),a.cancelBubble=!0,a.returnValue=!1},_mousemove:function(a){if(!this.options.enabled)return!1;var b=this._getPercentage(a);this._adjustPercentageForRangeSliders(b),this.percentage[this.dragged]=this.options.reversed?100-b:b,this._layout();var c=this._calculateValue(!0);return this.setValue(c,!0,!0),!1},_adjustPercentageForRangeSliders:function(a){if(this.options.range){var b=this._getNumDigitsAfterDecimalPlace(a);b=b?b-1:0;var c=this._applyToFixedAndParseFloat(a,b);0===this.dragged&&this._applyToFixedAndParseFloat(this.percentage[1],b)<c?(this.percentage[0]=this.percentage[1],this.dragged=1):1===this.dragged&&this._applyToFixedAndParseFloat(this.percentage[0],b)>c&&(this.percentage[1]=this.percentage[0],this.dragged=0)}},_mouseup:function(){if(!this.options.enabled)return!1;this.touchCapable&&(document.removeEventListener("touchmove",this.mousemove,!1),document.removeEventListener("touchend",this.mouseup,!1)),document.removeEventListener("mousemove",this.mousemove,!1),document.removeEventListener("mouseup",this.mouseup,!1),this.inDrag=!1,this.over===!1&&this._hideTooltip();var a=this._calculateValue(!0);return this._layout(),this._trigger("slideStop",a),this._setDataVal(a),!1},_calculateValue:function(a){var b;if(this.options.range?(b=[this.options.min,this.options.max],0!==this.percentage[0]&&(b[0]=this._toValue(this.percentage[0]),b[0]=this._applyPrecision(b[0])),100!==this.percentage[1]&&(b[1]=this._toValue(this.percentage[1]),b[1]=this._applyPrecision(b[1]))):(b=this._toValue(this.percentage[0]),b=parseFloat(b),b=this._applyPrecision(b)),a){for(var c=[b,1/0],d=0;d<this.options.ticks.length;d++){var e=Math.abs(this.options.ticks[d]-b);e<=c[1]&&(c=[this.options.ticks[d],e])}if(c[1]<=this.options.ticks_snap_bounds)return c[0]}return b},_applyPrecision:function(a){var b=this.options.precision||this._getNumDigitsAfterDecimalPlace(this.options.step);return this._applyToFixedAndParseFloat(a,b)},_getNumDigitsAfterDecimalPlace:function(a){var b=(""+a).match(/(?:\.(\d+))?(?:[eE]([+-]?\d+))?$/);return b?Math.max(0,(b[1]?b[1].length:0)-(b[2]?+b[2]:0)):0},_applyToFixedAndParseFloat:function(a,b){var c=a.toFixed(b);return parseFloat(c)},_getPercentage:function(a){!this.touchCapable||"touchstart"!==a.type&&"touchmove"!==a.type||(a=a.touches[0]);var b=a[this.mousePos],c=this.offset[this.stylePos],d=b-c,e=d/this.size*100;return e=Math.round(e/this.percentage[2])*this.percentage[2],Math.max(0,Math.min(100,e))},_validateInputValue:function(a){if("number"==typeof a)return a;if(Array.isArray(a))return this._validateArray(a),a;throw new Error(d.formatInvalidInputErrorMsg(a))},_validateArray:function(a){for(var b=0;b<a.length;b++){var c=a[b];if("number"!=typeof c)throw new Error(d.formatInvalidInputErrorMsg(c))}},_setDataVal:function(a){var b="value: '"+a+"'";this.element.setAttribute("data",b),this.element.setAttribute("value",a),this.element.value=a},_trigger:function(b,c){c=c||0===c?c:void 0;var d=this.eventToCallbackMap[b];if(d&&d.length)for(var e=0;e<d.length;e++){var f=d[e];f(c)}a&&this._triggerJQueryEvent(b,c)},_triggerJQueryEvent:function(a,b){var c={type:a,value:b};this.$element.trigger(c),this.$sliderElem.trigger(c)},_unbindJQueryEventHandlers:function(){this.$element.off(),this.$sliderElem.off()},_setText:function(a,b){"undefined"!=typeof a.innerText?a.innerText=b:"undefined"!=typeof a.textContent&&(a.textContent=b)},_removeClass:function(a,b){for(var c=b.split(" "),d=a.className,e=0;e<c.length;e++){var f=c[e],g=new RegExp("(?:\\s|^)"+f+"(?:\\s|$)");d=d.replace(g," ")}a.className=d.trim()},_addClass:function(a,b){for(var c=b.split(" "),d=a.className,e=0;e<c.length;e++){var f=c[e],g=new RegExp("(?:\\s|^)"+f+"(?:\\s|$)"),h=g.test(d);h||(d+=" "+f)}a.className=d.trim()},_offsetLeft:function(a){for(var b=a.offsetLeft;(a=a.offsetParent)&&!isNaN(a.offsetLeft);)b+=a.offsetLeft;return b},_offsetTop:function(a){for(var b=a.offsetTop;(a=a.offsetParent)&&!isNaN(a.offsetTop);)b+=a.offsetTop;return b},_offset:function(a){return{left:this._offsetLeft(a),top:this._offsetTop(a)}},_css:function(b,c,d){if(a)a.style(b,c,d);else{var e=c.replace(/^-ms-/,"ms-").replace(/-([\da-z])/gi,function(a,b){return b.toUpperCase()});b.style[e]=d}},_toValue:function(a){return this.options.scale.toValue.apply(this,[a])},_toPercentage:function(a){return this.options.scale.toPercentage.apply(this,[a])}},a){var f=a.fn.slider?"bootstrapSlider":"slider";a.bridget(f,b)}}(a),b});
                
/* ========================================================================
 * bootstrap-switch - v3.3.2
 * http://www.bootstrap-switch.org
 * ========================================================================
 * Copyright 2012-2013 Mattia Larentis
 *
 * ========================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ========================================================================
 */

(function(){var t=[].slice;!function(e,i){"use strict";var n;return n=function(){function t(t,i){null==i&&(i={}),this.$element=e(t),this.options=e.extend({},e.fn.bootstrapSwitch.defaults,{state:this.$element.is(":checked"),size:this.$element.data("size"),animate:this.$element.data("animate"),disabled:this.$element.is(":disabled"),readonly:this.$element.is("[readonly]"),indeterminate:this.$element.data("indeterminate"),inverse:this.$element.data("inverse"),radioAllOff:this.$element.data("radio-all-off"),onColor:this.$element.data("on-color"),offColor:this.$element.data("off-color"),onText:this.$element.data("on-text"),offText:this.$element.data("off-text"),labelText:this.$element.data("label-text"),handleWidth:this.$element.data("handle-width"),labelWidth:this.$element.data("label-width"),baseClass:this.$element.data("base-class"),wrapperClass:this.$element.data("wrapper-class")},i),this.$wrapper=e("<div>",{"class":function(t){return function(){var e;return e=[""+t.options.baseClass].concat(t._getClasses(t.options.wrapperClass)),e.push(t.options.state?""+t.options.baseClass+"-on":""+t.options.baseClass+"-off"),null!=t.options.size&&e.push(""+t.options.baseClass+"-"+t.options.size),t.options.disabled&&e.push(""+t.options.baseClass+"-disabled"),t.options.readonly&&e.push(""+t.options.baseClass+"-readonly"),t.options.indeterminate&&e.push(""+t.options.baseClass+"-indeterminate"),t.options.inverse&&e.push(""+t.options.baseClass+"-inverse"),t.$element.attr("id")&&e.push(""+t.options.baseClass+"-id-"+t.$element.attr("id")),e.join(" ")}}(this)()}),this.$container=e("<div>",{"class":""+this.options.baseClass+"-container"}),this.$on=e("<span>",{html:this.options.onText,"class":""+this.options.baseClass+"-handle-on "+this.options.baseClass+"-"+this.options.onColor}),this.$off=e("<span>",{html:this.options.offText,"class":""+this.options.baseClass+"-handle-off "+this.options.baseClass+"-"+this.options.offColor}),this.$label=e("<span>",{html:this.options.labelText,"class":""+this.options.baseClass+"-label"}),this.$element.on("init.bootstrapSwitch",function(e){return function(){return e.options.onInit.apply(t,arguments)}}(this)),this.$element.on("switchChange.bootstrapSwitch",function(e){return function(){return e.options.onSwitchChange.apply(t,arguments)}}(this)),this.$container=this.$element.wrap(this.$container).parent(),this.$wrapper=this.$container.wrap(this.$wrapper).parent(),this.$element.before(this.options.inverse?this.$off:this.$on).before(this.$label).before(this.options.inverse?this.$on:this.$off),this.options.indeterminate&&this.$element.prop("indeterminate",!0),this._init(),this._elementHandlers(),this._handleHandlers(),this._labelHandlers(),this._formHandler(),this._externalLabelHandler(),this.$element.trigger("init.bootstrapSwitch")}return t.prototype._constructor=t,t.prototype.state=function(t,e){return"undefined"==typeof t?this.options.state:this.options.disabled||this.options.readonly?this.$element:this.options.state&&!this.options.radioAllOff&&this.$element.is(":radio")?this.$element:(this.options.indeterminate&&this.indeterminate(!1),t=!!t,this.$element.prop("checked",t).trigger("change.bootstrapSwitch",e),this.$element)},t.prototype.toggleState=function(t){return this.options.disabled||this.options.readonly?this.$element:this.options.indeterminate?(this.indeterminate(!1),this.state(!0)):this.$element.prop("checked",!this.options.state).trigger("change.bootstrapSwitch",t)},t.prototype.size=function(t){return"undefined"==typeof t?this.options.size:(null!=this.options.size&&this.$wrapper.removeClass(""+this.options.baseClass+"-"+this.options.size),t&&this.$wrapper.addClass(""+this.options.baseClass+"-"+t),this._width(),this._containerPosition(),this.options.size=t,this.$element)},t.prototype.animate=function(t){return"undefined"==typeof t?this.options.animate:(t=!!t,t===this.options.animate?this.$element:this.toggleAnimate())},t.prototype.toggleAnimate=function(){return this.options.animate=!this.options.animate,this.$wrapper.toggleClass(""+this.options.baseClass+"-animate"),this.$element},t.prototype.disabled=function(t){return"undefined"==typeof t?this.options.disabled:(t=!!t,t===this.options.disabled?this.$element:this.toggleDisabled())},t.prototype.toggleDisabled=function(){return this.options.disabled=!this.options.disabled,this.$element.prop("disabled",this.options.disabled),this.$wrapper.toggleClass(""+this.options.baseClass+"-disabled"),this.$element},t.prototype.readonly=function(t){return"undefined"==typeof t?this.options.readonly:(t=!!t,t===this.options.readonly?this.$element:this.toggleReadonly())},t.prototype.toggleReadonly=function(){return this.options.readonly=!this.options.readonly,this.$element.prop("readonly",this.options.readonly),this.$wrapper.toggleClass(""+this.options.baseClass+"-readonly"),this.$element},t.prototype.indeterminate=function(t){return"undefined"==typeof t?this.options.indeterminate:(t=!!t,t===this.options.indeterminate?this.$element:this.toggleIndeterminate())},t.prototype.toggleIndeterminate=function(){return this.options.indeterminate=!this.options.indeterminate,this.$element.prop("indeterminate",this.options.indeterminate),this.$wrapper.toggleClass(""+this.options.baseClass+"-indeterminate"),this._containerPosition(),this.$element},t.prototype.inverse=function(t){return"undefined"==typeof t?this.options.inverse:(t=!!t,t===this.options.inverse?this.$element:this.toggleInverse())},t.prototype.toggleInverse=function(){var t,e;return this.$wrapper.toggleClass(""+this.options.baseClass+"-inverse"),e=this.$on.clone(!0),t=this.$off.clone(!0),this.$on.replaceWith(t),this.$off.replaceWith(e),this.$on=t,this.$off=e,this.options.inverse=!this.options.inverse,this.$element},t.prototype.onColor=function(t){var e;return e=this.options.onColor,"undefined"==typeof t?e:(null!=e&&this.$on.removeClass(""+this.options.baseClass+"-"+e),this.$on.addClass(""+this.options.baseClass+"-"+t),this.options.onColor=t,this.$element)},t.prototype.offColor=function(t){var e;return e=this.options.offColor,"undefined"==typeof t?e:(null!=e&&this.$off.removeClass(""+this.options.baseClass+"-"+e),this.$off.addClass(""+this.options.baseClass+"-"+t),this.options.offColor=t,this.$element)},t.prototype.onText=function(t){return"undefined"==typeof t?this.options.onText:(this.$on.html(t),this._width(),this._containerPosition(),this.options.onText=t,this.$element)},t.prototype.offText=function(t){return"undefined"==typeof t?this.options.offText:(this.$off.html(t),this._width(),this._containerPosition(),this.options.offText=t,this.$element)},t.prototype.labelText=function(t){return"undefined"==typeof t?this.options.labelText:(this.$label.html(t),this._width(),this.options.labelText=t,this.$element)},t.prototype.handleWidth=function(t){return"undefined"==typeof t?this.options.handleWidth:(this.options.handleWidth=t,this._width(),this._containerPosition(),this.$element)},t.prototype.labelWidth=function(t){return"undefined"==typeof t?this.options.labelWidth:(this.options.labelWidth=t,this._width(),this._containerPosition(),this.$element)},t.prototype.baseClass=function(){return this.options.baseClass},t.prototype.wrapperClass=function(t){return"undefined"==typeof t?this.options.wrapperClass:(t||(t=e.fn.bootstrapSwitch.defaults.wrapperClass),this.$wrapper.removeClass(this._getClasses(this.options.wrapperClass).join(" ")),this.$wrapper.addClass(this._getClasses(t).join(" ")),this.options.wrapperClass=t,this.$element)},t.prototype.radioAllOff=function(t){return"undefined"==typeof t?this.options.radioAllOff:(t=!!t,t===this.options.radioAllOff?this.$element:(this.options.radioAllOff=t,this.$element))},t.prototype.onInit=function(t){return"undefined"==typeof t?this.options.onInit:(t||(t=e.fn.bootstrapSwitch.defaults.onInit),this.options.onInit=t,this.$element)},t.prototype.onSwitchChange=function(t){return"undefined"==typeof t?this.options.onSwitchChange:(t||(t=e.fn.bootstrapSwitch.defaults.onSwitchChange),this.options.onSwitchChange=t,this.$element)},t.prototype.destroy=function(){var t;return t=this.$element.closest("form"),t.length&&t.off("reset.bootstrapSwitch").removeData("bootstrap-switch"),this.$container.children().not(this.$element).remove(),this.$element.unwrap().unwrap().off(".bootstrapSwitch").removeData("bootstrap-switch"),this.$element},t.prototype._width=function(){var t,e;return t=this.$on.add(this.$off),t.add(this.$label).css("width",""),e="auto"===this.options.handleWidth?Math.max(this.$on.width(),this.$off.width()):this.options.handleWidth,t.width(e),this.$label.width(function(t){return function(i,n){return"auto"!==t.options.labelWidth?t.options.labelWidth:e>n?e:n}}(this)),this._handleWidth=this.$on.outerWidth(),this._labelWidth=this.$label.outerWidth(),this.$container.width(2*this._handleWidth+this._labelWidth),this.$wrapper.width(this._handleWidth+this._labelWidth)},t.prototype._containerPosition=function(t,e){return null==t&&(t=this.options.state),this.$container.css("margin-left",function(e){return function(){var i;return i=[0,"-"+e._handleWidth+"px"],e.options.indeterminate?"-"+e._handleWidth/2+"px":t?e.options.inverse?i[1]:i[0]:e.options.inverse?i[0]:i[1]}}(this)),e?setTimeout(function(){return e()},50):void 0},t.prototype._init=function(){var t,e;return t=function(t){return function(){return t._width(),t._containerPosition(null,function(){return t.options.animate?t.$wrapper.addClass(""+t.options.baseClass+"-animate"):void 0})}}(this),this.$wrapper.is(":visible")?t():e=i.setInterval(function(n){return function(){return n.$wrapper.is(":visible")?(t(),i.clearInterval(e)):void 0}}(this),50)},t.prototype._elementHandlers=function(){return this.$element.on({"change.bootstrapSwitch":function(t){return function(i,n){var o;return i.preventDefault(),i.stopImmediatePropagation(),o=t.$element.is(":checked"),t._containerPosition(o),o!==t.options.state?(t.options.state=o,t.$wrapper.toggleClass(""+t.options.baseClass+"-off").toggleClass(""+t.options.baseClass+"-on"),n?void 0:(t.$element.is(":radio")&&e("[name='"+t.$element.attr("name")+"']").not(t.$element).prop("checked",!1).trigger("change.bootstrapSwitch",!0),t.$element.trigger("switchChange.bootstrapSwitch",[o]))):void 0}}(this),"focus.bootstrapSwitch":function(t){return function(e){return e.preventDefault(),t.$wrapper.addClass(""+t.options.baseClass+"-focused")}}(this),"blur.bootstrapSwitch":function(t){return function(e){return e.preventDefault(),t.$wrapper.removeClass(""+t.options.baseClass+"-focused")}}(this),"keydown.bootstrapSwitch":function(t){return function(e){if(e.which&&!t.options.disabled&&!t.options.readonly)switch(e.which){case 37:return e.preventDefault(),e.stopImmediatePropagation(),t.state(!1);case 39:return e.preventDefault(),e.stopImmediatePropagation(),t.state(!0)}}}(this)})},t.prototype._handleHandlers=function(){return this.$on.on("click.bootstrapSwitch",function(t){return function(e){return e.preventDefault(),e.stopPropagation(),t.state(!1),t.$element.trigger("focus.bootstrapSwitch")}}(this)),this.$off.on("click.bootstrapSwitch",function(t){return function(e){return e.preventDefault(),e.stopPropagation(),t.state(!0),t.$element.trigger("focus.bootstrapSwitch")}}(this))},t.prototype._labelHandlers=function(){return this.$label.on({"mousedown.bootstrapSwitch touchstart.bootstrapSwitch":function(t){return function(e){return t._dragStart||t.options.disabled||t.options.readonly?void 0:(e.preventDefault(),e.stopPropagation(),t._dragStart=(e.pageX||e.originalEvent.touches[0].pageX)-parseInt(t.$container.css("margin-left"),10),t.options.animate&&t.$wrapper.removeClass(""+t.options.baseClass+"-animate"),t.$element.trigger("focus.bootstrapSwitch"))}}(this),"mousemove.bootstrapSwitch touchmove.bootstrapSwitch":function(t){return function(e){var i;if(null!=t._dragStart&&(e.preventDefault(),i=(e.pageX||e.originalEvent.touches[0].pageX)-t._dragStart,!(i<-t._handleWidth||i>0)))return t._dragEnd=i,t.$container.css("margin-left",""+t._dragEnd+"px")}}(this),"mouseup.bootstrapSwitch touchend.bootstrapSwitch":function(t){return function(e){var i;if(t._dragStart)return e.preventDefault(),t.options.animate&&t.$wrapper.addClass(""+t.options.baseClass+"-animate"),t._dragEnd?(i=t._dragEnd>-(t._handleWidth/2),t._dragEnd=!1,t.state(t.options.inverse?!i:i)):t.state(!t.options.state),t._dragStart=!1}}(this),"mouseleave.bootstrapSwitch":function(t){return function(){return t.$label.trigger("mouseup.bootstrapSwitch")}}(this)})},t.prototype._externalLabelHandler=function(){var t;return t=this.$element.closest("label"),t.on("click",function(e){return function(i){return i.preventDefault(),i.stopImmediatePropagation(),i.target===t[0]?e.toggleState():void 0}}(this))},t.prototype._formHandler=function(){var t;return t=this.$element.closest("form"),t.data("bootstrap-switch")?void 0:t.on("reset.bootstrapSwitch",function(){return i.setTimeout(function(){return t.find("input").filter(function(){return e(this).data("bootstrap-switch")}).each(function(){return e(this).bootstrapSwitch("state",this.checked)})},1)}).data("bootstrap-switch",!0)},t.prototype._getClasses=function(t){var i,n,o,s;if(!e.isArray(t))return[""+this.options.baseClass+"-"+t];for(n=[],o=0,s=t.length;s>o;o++)i=t[o],n.push(""+this.options.baseClass+"-"+i);return n},t}(),e.fn.bootstrapSwitch=function(){var i,o,s;return o=arguments[0],i=2<=arguments.length?t.call(arguments,1):[],s=this,this.each(function(){var t,a;return t=e(this),a=t.data("bootstrap-switch"),a||t.data("bootstrap-switch",a=new n(this,o)),"string"==typeof o?s=a[o].apply(a,i):void 0}),s},e.fn.bootstrapSwitch.Constructor=n,e.fn.bootstrapSwitch.defaults={state:!0,size:null,animate:!0,disabled:!1,readonly:!1,indeterminate:!1,inverse:!1,radioAllOff:!1,onColor:"primary",offColor:"default",onText:"ON",offText:"OFF",labelText:"&nbsp;",handleWidth:"auto",labelWidth:"auto",baseClass:"bootstrap-switch",wrapperClass:"wrapper",onInit:function(){},onSwitchChange:function(){}}}(window.jQuery,window)}).call(this);