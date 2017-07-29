(function sbcc($) {
 function highestIndex($items) {
  var itemIndex = 0;
  $items.each(function(i, item) {
   if ($(item).data('index') >= itemIndex) {
    itemIndex = $(item).data('index');
   }
  });
  return itemIndex;
 }

 $('.sbcc.removeGroup').live('click',function() {
  $group = $(this).closest('.group');
  $group.remove();
 });

 $('.sbcc.newGroup').live('click',function() {
  $sbcc = $(this).closest('.sbcc');
  $groups = $('.groups',$sbcc);
  var groupIndex = highestIndex($('.group',$groups)) + 1;
  $groups.prepend(se.bjurr.group({'group':groupIndex,'config':[],'errors':[]}));
 });

 $('.sbcc.removeRule').live('click',function() {
  $rule = $(this).closest('.rule');
  $rule.remove();
 });

 $('.sbcc.newRule').live('click',function() {
  $group = $(this).closest('.group');
  var groupIndex = $group.data('index');
  $rules = $('.rules',$group);
  var ruleIndex = highestIndex($('.rule',$rules)) + 1;
  $rules.prepend(se.bjurr.rule({'rule':ruleIndex,'group':groupIndex,'config':[],'errors':[]}));
 });
})(jQuery);
