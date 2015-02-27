(function sscc($) {
 function highestIndex($items) {
  var itemIndex = 0;
  $items.each(function(i, item) {
   if ($(item).data('index') >= itemIndex) {
    itemIndex = $(item).data('index');
   }
  });
  return itemIndex;
 }

 $('.removeGroup').live('click',function() {
  $group = $(this).closest('.group');
  $group.remove();
 });

 $('.newGroup').live('click',function() {
  $sscc = $(this).closest('.sscc');
  $groups = $('.groups',$sscc);
  var groupIndex = highestIndex($('.group',$groups)) + 1;
  $groups.prepend(se.bjurr.group({'group':groupIndex,'config':[],'errors':[]}));
 });

 $('.removeRule').live('click',function() {
  $rule = $(this).closest('.rule');
  $rule.remove();
 });

 $('.newRule').live('click',function() {
  $group = $(this).closest('.group');
  var groupIndex = $group.data('index');
  $rules = $('.rules',$group);
  var ruleIndex = highestIndex($('.rule',$rules)) + 1;
  $rules.prepend(se.bjurr.rule({'rule':ruleIndex,'group':groupIndex,'config':[],'errors':[]}));
 });
})(jQuery);
