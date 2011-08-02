function openHTMLEditor(content){
	htmlEditorContent = unescape(content); 
	$.colorbox({
		'iframe':true, 
		href:'assets/html-editor/editor.html',
		'width':'90%', 
		'height':'90%',
		'overlayClose':false,
		'escKey':false,
		'fastIframe':false});
}