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

$('#twitter').click -> popup('https://twitter.com/home?status=I%20just%20found%20a%20new%20sound%20customization%20app%20called%20@blipsqueak!%20Check%20it%20out%20at%20http://blipsqueak.io')
$('#google-plus').click -> popup('https://plus.google.com/share?url=http%3A%2F%2Fblipsqueak.io')
$('#tumblr').click -> popup('http://www.tumblr.com/share/link?url=http%3A%2F%2Fblipsqueak.io&name=Blipsqueak&description=I%20just%20found%20a%20new%20Android%20app%20for%20customizing%20notification%20sounds%20called%20Blipsqueak.%20Check%20it%20out!')
$('#facebook').click -> popup('https://www.facebook.com/sharer/sharer.php?u=http://blipsqueak.io')
$('#share').click -> toggleActionMenu()
