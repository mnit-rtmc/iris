function hide(elem_id) {
	document.getElementById(elem_id).style.display = 'none';
}
function show(elem_id) {
	document.getElementById(elem_id).style.display = '';
}
function bindShowOnly(sections, sec) {
	document.getElementById(sec + '_link').onclick = function() {
		for(var i = 0; i < sections.length; i++) {
			if(sections[i] == sec) {
				show(sections[i] + '_div');
			} else {
				hide(sections[i] + '_div');
			}
		}
	}
}
function bindClicks() {
	var sections = ['contents', 'overview', 'installation', 'basic_setup',
		'devices', 'features', 'troubleshooting', 'maintenance',
		'development', 'stepbystep'];
	for(var i = 0; i < sections.length; i++) {
		hide(sections[i] + '_div');
		bindShowOnly(sections, sections[i]);
	}
}

window.onload = bindClicks;
