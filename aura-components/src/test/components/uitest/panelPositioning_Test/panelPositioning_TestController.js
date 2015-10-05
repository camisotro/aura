({
	init: function() {

	},

    handleMouseDown: function(cmp, evt, helper) {
        helper.handleMouseDown(cmp, evt);
    },

	handlePress: function(cmp, evt, helper) {
		var body = $A.newCmp({componentDef: 'aura:unescapedHtml', attributes: {values: {value: '<div class="panel-content">Benjamin is a whale</div>'}}})
		var bigTarget = cmp.find('bigTarget').getElement();
		var littleTarget = cmp.find('littleTarget').getElement();
        var value = cmp.find('direction').get('v.value');
        var pad = parseInt(cmp.find('pad').get('v.value'),10);


        var isInside = cmp.find('isInside').get('v.value');

        var panelConfig ={
            referenceElement: isInside ? bigTarget : littleTarget,
            showCloseButton: false,
            closeOnClickOut: true,
            useTransition: false,
            body  : body,
            direction: value,
            showPointer: false,
            boundingElement: isInside ? window : bigTarget,
            inside: isInside,
            pad: pad
        };

        if(cmp.find('isAdvanced').get('v.value')) {
            delete panelConfig.direction;
            panelConfig.advanced = true;
            panelConfig.align = cmp.find('align').get('v.value');
            panelConfig.targetAlign = cmp.find('targetAlign').get('v.value');
        }

		$A.get('e.ui:createPanel').setParams({
            panelType   :'panel',
            visible: true,
            panelConfig : panelConfig
        }).fire();
	}
})