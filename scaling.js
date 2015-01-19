(function() {
  var fadeInOut, fadePhoneInOut, name, onResize, scaleThings, waypoint, waypoints;

  fadeInOut = function(direction) {
    if (direction === 'down') {
      return $(this.element).find('.lightup').animate({
        opacity: 1
      }, 1000);
    } else {
      return $(this.element).find('.lightup').animate({
        opacity: 0
      }, 1000);
    }
  };

  fadePhoneInOut = function(direction) {
    if (direction === 'up') {
      return $('.segment').css({
        display: 'block'
      }).animate({
        opacity: 1
      }, 1000);
    } else {
      return $('.segment').animate({
        opacity: 0
      }, 1000);
    }
  };

  waypoints = {
    table: {
      handler: fadeInOut,
      offset: '60%'
    },
    pocket: {
      handler: fadeInOut,
      offset: '50%'
    },
    carseat: {
      handler: fadeInOut,
      offset: '40%'
    },
    fadespacer: {
      handler: fadePhoneInOut,
      offset: '35%'
    },
    notification: {
      handler: fadeInOut,
      offset: '25%'
    },
    pling: {
      handler: function(direction) {
        if (direction === 'down') {
          return $('#pling').animate({
            opacity: 1
          }, 500).animate({
            opacity: 0
          }, 2000);
        }
      },
      offset: '25%'
    },
    features: {
      handler: function() {
        return $('.segment').css({
          display: 'none'
        });
      }
    }
  };

  for (name in waypoints) {
    waypoint = waypoints[name];
    if (waypoint.element == null) {
      waypoint.element = document.getElementById(name);
    }
    if (waypoint.offset == null) {
      waypoint.offset = '50%';
    }
    new Waypoint(waypoint);
  }

  onResize = function() {
    if ($(window).width() >= 751) {
      $('#feature1').attr('src', './img/feature-2.png');
      return $('#feature2').attr('src', './img/feature-1.png');
    } else {
      $('#feature1').attr('src', './img/feature-1.png');
      return $('#feature2').attr('src', './img/feature-2.png');
    }
  };

  scaleThings = function() {
    var imgholders, phoneHeight, ps;
    imgholders = document.getElementsByClassName('imgholder');
    Array.prototype.forEach.call(imgholders, function(holder) {
      var child, newwidth, _results;
      holder.style.height = holder.offsetHeight + 'px';
      newwidth = holder.offsetWidth + 'px';
      child = holder.firstChild;
      _results = [];
      while (child !== null && child.tagName === 'img') {
        child.style.width = newwidth;
        _results.push(child = child.nextSibling);
      }
      return _results;
    });
    phoneHeight = 0;
    $('.segment').each(function(i, el) {
      return phoneHeight += el.height;
    });
    if (phoneHeight - $(window).height() < 113) {
      $(window).stuck();
    }
    ps = document.getElementsByTagName('p');
    Array.prototype.forEach.call(ps, function(p) {
      p.style.height = p.parentNode.offsetHeight + 'px';
      return p.style.width = p.parentNode.offsetWidth + 'px';
    });
    if ($(window).width() >= 768) {
      $('#table').css({
        'position': 'relative',
        'top': '0vh'
      });
      $('#pocket').css({
        'position': 'relative',
        'top': '10vh'
      });
      $('#carseat').css({
        'position': 'relative',
        'top': '20vh'
      });
    }
    return onResize();
  };

  window.addEventListener('load', scaleThings);

  window.addEventListener('orientationchange', scaleThings);

  window.addEventListener('resize', scaleThings);

  document.getElementById('ideas').onclick = function(event) {
    var link, links, options, target;
    event = event || window.event;
    target = event.target || event.srcElement;
    link = target.src ? target.parentNode : target;
    options = {
      index: link,
      event: event
    };
    links = this.getElementsByTagName('a');
    return blueimp.Gallery(links, options);
  };

}).call(this);
