/*
 * Copyright (C) 2013 salesforce.com, inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 // Code fom:
 // https://github.com/davidtheclark/tabbable
 // https://github.com/davidtheclark/focus-trap

function lib() {
    var trap;
    var tabbableNodes;
    var previouslyFocused;
    var activeFocusTrap;
    var config;

    function tabbable(el) {
        var basicTabbables = [];
        var orderedTabbables = [];
        var candidateNodelist = el.querySelectorAll('input, select, a, textarea, button, [tabindex]');
        var candidates = Array.prototype.slice.call(candidateNodelist);
        var candidate, candidateIndex;

        for (var i = 0, l = candidates.length; i < l; i++) {
            candidate = candidates[i];
            candidateIndex = candidate.tabIndex;

            if (candidateIndex < 0 || (candidate.tagName === 'INPUT' && candidate.type === 'hidden') || (candidate.tagName === 'A' && !candidate.href && !candidate.tabIndex) || candidate.disabled || isHidden(candidate)) {
              continue;
            }

            if (candidateIndex === 0) {
                basicTabbables.push(candidate);
            } else {
                orderedTabbables.push({
                    tabIndex: candidateIndex,
                    node: candidate
                });
            }
        }

        var tabbableNodes = orderedTabbables
            .sort(function(a, b) {
                return a.tabIndex - b.tabIndex;
            })
            .map(function(a) {
                return a.node;
            });

        Array.prototype.push.apply(tabbableNodes, basicTabbables);
        return tabbableNodes;
    }

    var nodeCache = {};
    var nodeCacheIndex = 1;

    function isHidden(node) {
        if (node === document.documentElement) {
            return false;
        }

        if (node.tabbableCacheIndex) {
            return nodeCache[node.tabbableCacheIndex];
        }

        var result = false;
        var style = window.getComputedStyle(node);
        if (style.visibility === 'hidden' || style.display === 'none') {
            result = true;
        } else if (node.parentNode) {
            result = isHidden(node.parentNode);
        }

        node.tabbableCacheIndex = nodeCacheIndex;
        nodeCache[node.tabbableCacheIndex] = result;
        nodeCacheIndex++;

        return result;
    }

    function activate(element, options) {
        // There can be only one focus trap at a time
        if (activeFocusTrap) { 
            deactivate();
        }

        activeFocusTrap = true;

        trap = (typeof element === 'string') ? document.querySelector(element) : element;
        config = options || {};
        previouslyFocused = document.activeElement;

        updateTabbableNodes();

        var focusNode = (function() {
            var node;
            if (!config.initialFocus) {
                node = tabbableNodes[0];
                if (!node) {
                    throw new Error('You can\'t have a focus-trap without at least one focusable element');
                }
              return node;
            }

            if (typeof config.initialFocus === 'string') {
                node = document.querySelector(config.initialFocus);
            } else {
                node = config.initialFocus;
            }

            if (!node) {
              throw new Error('The `initialFocus` selector you passed referred to no known node');
            }
            return node;
        }());

        focusNode.focus();

        document.addEventListener('focus', checkFocus, true);
        document.addEventListener('click', checkClick, true);
        document.addEventListener('touchend', checkClick, true);
        document.addEventListener('keydown', checkKey, true);
    }

    function deactivate() {
        if (!activeFocusTrap) { 
            return; 
        }
        activeFocusTrap = false;

        document.removeEventListener('focus', checkFocus, true);
        document.removeEventListener('click', checkClick, true);
        document.removeEventListener('touchend', checkClick, true);
        document.removeEventListener('keydown', checkKey, true);

        if (config.onDeactivate) {
            config.onDeactivate();
        }

        setTimeout(function() {
            previouslyFocused.focus();
        }, 0);
    }

    function checkClick(e) {
        if (trap.contains(e.target)) { 
            return;
        }
        e.preventDefault();
        e.stopImmediatePropagation();
    }

    function checkFocus(e) {
        updateTabbableNodes();
        if (trap.contains(e.target)){
            return;
        }
        tabbableNodes[0].focus();
    }

    function checkKey(e) {
        if (e.key === 'Tab' || e.keyCode === 9) {
            e.preventDefault();
            updateTabbableNodes();
            var currentFocusIndex = tabbableNodes.indexOf(e.target);
            var lastTabbableNode = tabbableNodes[tabbableNodes.length - 1];
            var firstTabbableNode = tabbableNodes[0];
            if (e.shiftKey) {
                if (e.target === firstTabbableNode) {
                    lastTabbableNode.focus();
                    return;
                }
                tabbableNodes[currentFocusIndex - 1].focus(0);
                return;
            }
            if (e.target === lastTabbableNode) {
                firstTabbableNode.focus();
                return;
            }
            tabbableNodes[currentFocusIndex + 1].focus();
        }

        if (e.key === 'Escape' || e.key === 'Esc' || e.keyCode === 27) {
            deactivate();
        }
    }

    function updateTabbableNodes() {
        tabbableNodes = tabbable(trap);
    }

    return {
        activate   : activate,
        deactivate : deactivate
    };
}