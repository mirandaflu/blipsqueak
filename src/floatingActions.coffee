window.popup = (url) ->
	newwindow = window.open(url,'Share Blipsqueak','height=300,width=500');
	if window.focus then newwindow.focus()
	return false;

window.toggleActionMenu = ->
	if $('#twitter.button').css('opacity') < 1
		numButtons = $('.button').length
		anim_length = 400 #milliseconds
		for i in [numButtons-1..0]
			button = $('.button')[i]
			$(button).animate {opacity:1, top:'-'+((numButtons - i)*66)+'px'}, anim_length - 50*i
		$('.fa-share-alt').hide()
		$('.fa-times').show()
	else
		$('.button').animate {opacity:0, top:0}, 300
		$('.fa-share-alt').show()
		$('.fa-times').hide()
