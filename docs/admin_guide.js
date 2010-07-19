function hide(elem_id) {
	document.getElementById(elem_id).style.display = 'none';
}
function show(elem_id) {
	document.getElementById(elem_id).style.display = '';
}
function showOnly(sections, sec) {
	document.getElementById(sec + '_link').onclick = function() {
		for(var i = 0; i < sections.length; i++) {
			if(sections[i] == sec) {
				show(sections[i] + '_div');
				hide(sections[i] + '_show');
				show(sections[i] + '_hide');
			} else {
				hide(sections[i] + '_div');
				show(sections[i] + '_show');
				hide(sections[i] + '_hide');
			}
		}
	}
}
function bindShowHide(sec) {
	document.getElementById(sec + '_show').onclick = function() {
		show(sec + '_div');
		hide(sec + '_show');
		show(sec + '_hide');
	}
	document.getElementById(sec + '_hide').onclick = function() {
		hide(sec + '_div');
		hide(sec + '_hide');
		show(sec + '_show');
	}
}
function bindClicks() {
	var sections = ['overview', 'installation', 'configuration',
		'troubleshooting', 'maintenance', 'development'];
	for(var i = 0; i < sections.length; i++) {
		hide(sections[i] + '_div');
		hide(sections[i] + '_hide');
		showOnly(sections, sections[i]);
		bindShowHide(sections[i]);
	}
}

window.onload = bindClicks;
