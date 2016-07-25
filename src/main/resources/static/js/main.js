$(document).ready(function() {
  // verify token is valid
  var tokenExpiration = localStorage.getItem("tokenExpiration");
  if (tokenExpiration === null || new Date().getTime() >= tokenExpiration) {
    localStorage.removeItem("id");
    localStorage.removeItem("token");
    localStorage.removeItem("name");
    localStorage.removeItem("tokenExpiration");

    if (location.pathname === "/dashboard.html") {
      document.location.href = "/";
    }
  } else if (document.location.href.indexOf("/dashboard.html") == -1) {
    document.location.href = '/dashboard.html';
  }

  $("#registerForm").submit(function(e) {
    var name = $('input[name=name]').val();
    var email = $('input[name=email]').val();
    var password = $('input[name=password]').val();

    $.ajax({
      url: "auth/register?name=" + name + "&email=" + email + "&password=" + password,
      type: "GET",
      crossDomain: true,
      success: function(data, textStatus, jqXHR) {
        alert('You have successfully registered, now please login!');
        document.location.href = '/';
      },
      error: function(xhr, textStatus, errorThrown) {
        switch (xhr.status) {
          case 406:
            alert('Password not strong enough. Must be at least 8 characters long and include both letters and digits!');
            break;
          case 409:
            alert('This email is already registered, please log in!');
            break;
          case 500:
            alert('Server failed, try again later!');
            break;
        }
      }
    });
    return false;
  });

  $("#loginForm").submit(function(e) {
    var email = $('input[name=email]').val();
    var password = $('input[name=password]').val();

    $.ajax({
      url: "auth/login?email=" + email + "&password=" + password,
      type: "GET",
      crossDomain: true,
      success: function(data, textStatus, jqXHR) {
        localStorage.setItem("id", data.user.id);
        localStorage.setItem("token", data.token);
        localStorage.setItem("name", data.user.name);
        localStorage.setItem("tokenExpiration", new Date().addMinutes(20).getTime());

        document.location.href = '/dashboard.html';
      },
      error: function(xhr, textStatus, errorThrown) {
        switch (xhr.status) {
          case 403:
            alert('Incorrect password');
            break;
          case 404:
            alert('User with that email not found');
            break;
          case 500:
            alert('Server failed, try again later!');
            break;
        }
      }
    });
    return false;
  });

  Date.prototype.addMinutes = function(minutes) {
    var copiedDate = new Date(this.getTime());
    return new Date(copiedDate.getTime() + minutes * 60000);
  };

});
