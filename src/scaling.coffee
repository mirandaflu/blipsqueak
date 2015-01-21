###
Copyright (C) 2015 Miranda Fluharty

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>
###

#waypoints
fadeInOut = (direction) ->
	if direction is 'down' then $(this.element).find('.lightup').animate {opacity:1}, 1000
	else $(this.element).find('.lightup').animate {opacity:0}, 1000
fadePhoneInOut = (direction) ->
	if direction is 'up' then $('.segment').css({display:'block';}).animate {opacity:1}, 1000
	else $('.segment').animate {opacity:0}, 1000
waypoints =
	table:
		handler: fadeInOut
		offset: '60%'
	pocket:
		handler: fadeInOut
		offset: '50%'
	carseat:
		handler: fadeInOut
		offset: '40%'
	fadespacer:
		handler: fadePhoneInOut
		offset: '35%'
	notification:
		handler: fadeInOut
		offset: '25%'
	pling:
		handler: (direction) -> $('#pling').animate({opacity:1}, 500).animate({opacity:0}, 2000) if direction is 'down'
		offset: '25%'
	features:
		handler: -> $('.segment').css({display:'none';})
for name, waypoint of waypoints
	waypoint.element ?= document.getElementById name
	waypoint.offset ?= '50%'
	new Waypoint waypoint

onResize = ->
	#top image responsiveness
	if $(window).width() >= 751
		$('#feature1').attr('src','./img/feature-2.png')
		$('#feature2').attr('src','./img/feature-1.png')
	else
		$('#feature1').attr('src','./img/feature-1.png')
		$('#feature2').attr('src','./img/feature-2.png')

scaleThings = ->
	#make sure nothing goes awry with the sticky elements
	imgholders = document.getElementsByClassName 'imgholder'
	Array.prototype.forEach.call imgholders, (holder) ->

		holder.style.height = holder.offsetHeight + 'px'

		newwidth = holder.offsetWidth + 'px';
		child = holder.firstChild
		while child isnt null and child.tagName is 'img'
			child.style.width = newwidth
			child = child.nextSibling

	#only use sticky elements if the browser is tall enough to hold the phone
	phoneHeight = 0
	$('.segment').each (i, el) -> phoneHeight += el.height
	$(window).stuck() if phoneHeight - $(window).height() < 113

	ps = document.getElementsByTagName 'p'
	Array.prototype.forEach.call ps, (p) ->
		p.style.height = p.parentNode.offsetHeight + 'px'
		p.style.width = p.parentNode.offsetWidth + 'px'

	if $(window).width() >= 768
		$('#pocket').css {'position':'relative', 'top': Math.floor(window.innerHeight * 0.1) + 'px'}
		$('#carseat').css {'position':'relative', 'top':Math.floor(window.innerHeight * 0.2) + 'px'}

	onResize()

window.addEventListener 'load', scaleThings
window.addEventListener 'orientationchange', scaleThings
window.addEventListener 'resize', scaleThings

#idea gallery
document.getElementById('ideas').onclick = (event) ->
	event = event or window.event
	target = event.target or event.srcElement
	return if target.tagName isnt 'IMG'
	link = if target.src then target.parentNode else target
	options = {index: link, event: event}
	links = this.getElementsByTagName 'a'
	blueimp.Gallery links, options
