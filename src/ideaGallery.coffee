document.getElementById('ideas').onclick = (event) ->
	event = event or window.event
	target = event.target or event.srcElement
	return if target.tagName isnt 'IMG'
	link = if target.src then target.parentNode else target
	options = {index: link, event: event}
	links = this.getElementsByTagName 'a'
	blueimp.Gallery links, options
