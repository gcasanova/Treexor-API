$(document).ready(function() {
  var id = localStorage.getItem("id");
  var token = localStorage.getItem("token");
  var name = localStorage.getItem("name");

  $('#welcomeMessage').html("Welcome " + name + "!");

  $.ajax({
    url: "discounts/retrieve",
    type: "GET",
    headers: {
      'Id': id,
      'Token': token
    },
    crossDomain: true,
    success: function(data, textStatus, jqXHR) {
      var html = '';
      for (var key in data) {
        if (data.hasOwnProperty(key)) {
          html += '<li>' + key + ' : ' + data[key] + '</li>';
        }
      }
      $("#discountCodes").html(html);
    },
    error: function(xhr, textStatus, errorThrown) {
      switch (xhr.status) {
        case 500:
          alert('Server failed, try again later!');
          break;
      }
    }
  });

  $("#logout").click(function() {
    localStorage.removeItem("id");
    localStorage.removeItem("token");
    localStorage.removeItem("name");
    localStorage.removeItem("tokenExpiration");

    document.location.href = "/";
  });

  $("#discountCodeCreationForm").submit(function(e) {
    var name = $('input[name=name]').val();

    $.ajax({
      url: "discounts/create?pharmacyName=" + name,
      type: "GET",
      headers: {
        'Id': id,
        'Token': token
      },
      crossDomain: true,
      success: function(data, textStatus, jqXHR) {
        alert('Discount code for pharmacy ' + name + ' created: ' + data);
        $('input[name=name]').val('');
        $("#discountCodes").append('<li>' + name + ' : ' + data + '</li>');
      },
      error: function(xhr, textStatus, errorThrown) {
        switch (xhr.status) {
          case 409:
            alert('This pharmacy already has a discount code!');
            $('input[name=name]').val('');
            break;
          case 500:
            alert('Server failed, try again later!');
            $('input[name=name]').val('');
            break;
        }
      }
    });
    return false;
  });

  $("#discountCodeVerificationForm").submit(function(e) {
    var code = $('input[name=code]').val();

    $.ajax({
      url: "discounts/verify?discountCode=" + code,
      type: "GET",
      headers: {
        'Id': id,
        'Token': token
      },
      crossDomain: true,
      success: function(data, textStatus, jqXHR) {
        alert('Discount code was successfully used');
        $('input[name=code]').val('');
      },
      error: function(xhr, textStatus, errorThrown) {
        switch (xhr.status) {
          case 404:
            alert('This discount code does not exist!');
            $('input[name=code]').val('');
            break;
          case 409:
            alert('You can only use a discount code one time!');
            $('input[name=code]').val('');
            break;
          case 500:
            alert('Server failed, try again later!');
            $('input[name=code]').val('');
            break;
        }
      }
    });
    return false;
  });

  $("#reportForm").submit(function(e) {
    var email = $('input[name=email]').val();

    $.ajax({
      url: "discounts/report?email=" + email,
      type: "GET",
      headers: {
        'Id': id,
        'Token': token
      },
      crossDomain: true,
      success: function(data, textStatus, jqXHR) {
        alert('Report requested successfully, check your inbox in a short time!');
        $('input[name=email]').val('');
      },
      error: function(xhr, textStatus, errorThrown) {
        switch (xhr.status) {
          case 500:
            alert('Server failed, try again later!');
            $('input[name=email]').val('');
            break;
        }
      }
    });
    return false;
  });

});
