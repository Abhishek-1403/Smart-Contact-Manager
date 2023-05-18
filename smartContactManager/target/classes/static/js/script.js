console.log("This is Script File");

const toggleSidebar = () => {
  if ($(".sidebar").is(":visible")) {
    // true - off the sidebar

    console.log("true field called........................");
    $(".sidebar").css("display", "none");
    $(".content").css("margin-left", "0%");
  } else {
    //false - show sidebar
    console.log(
      "false condition is called**********************************************************"
    );
    $(".sidebar").css("display", "block");
    $(".content").css("margin-left", "20%");
  }
};

const search = () => {
  //console.log("Searching...");

  let query = $("#search-input").val();
  console.log(query);

  if (query == "") {
    $(".search-result").hide();
  } else {
    console.log(query);

    // sending request to server

    let url = `http://localhost:8080/search/${query}`;

    fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((data) => {
        //data
        // console.log(data);

        let text = `<div class='list-group'>`;
        let uName = null;
        data.forEach((contact) => {
          if (contact.name != null) {
            text += `<a href='/user/${contact.email}/${uName}/contact' class='list-group-item list-group-item-action'>${contact.name} </a>`;
          } else {
            uName = contact.email;
          }
        });

        text += `</div>`;

        $(".search-result").html(text);
        $(".search-result").show();
      });

    $(".search-result").show();
  }
};

// First request- to server to create order

const paymentStart = () => {
  // console.log("payment started..");

  let amount = $("#payment_field").val();
  //console.log(amount);

  if (amount == "" || amount == null) {
    // alert("amount is required !!");
    swal("Failed !!", "Amount is required !!", "error");
    return;
  }

  // code... for request to server
  // we use Ajax to send request to server to create order - jquery

  $.ajax({
    url: "/payment/create_order",
    data: JSON.stringify({ amount: amount, info: "order_request" }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      // invoked when success
      console.log(response);
      if (response.status == "created") {
        // open payment form
        let options = {
          key: "rzp_test_gA8BZGXzwg9dUP",
          amount: response.amsount/100,
          currency: "INR",
          name: "Smart Contact Manager",
          description: "Support",
          image: "https://portfolio-website-92608.web.app/assets/image2.jpeg",
          order_id: response.id,
          handler: function (response) {
            console.log(response.razorpay_payment_id);
            console.log(response.razorpay_order_id);
            console.log(response.razorpay_signature);
            console.log("payment successfull !!");
            //alert("congrates !! payment successfull !!");

            updatePaymentOnServer(
              response.razorpay_payment_id,
              response.razorpay_order_id,
              'paid'
            );
          },
          prefill: {
            name: "",
            email: "",
            contact: "",
          },
          notes: {
            address: "Abhisheka's Enterprises Group",
          },
          theme: {
            color: "#3399cc",
          },
        };

        let rzp = new Razorpay(options);

        rzp.on("payment.failed", function (response) {
          console.log(response.error.code);
          console.log(response.error.description);
          console.log(response.error.source);
          console.log(response.error.step);
          console.log(response.error.reason);
          console.log(response.error.metadata.order_id);
          console.log(response.error.metadata.payment_id);
          alert("Oops payment failed..");
          swal("Failed !!", "Oops payment failed.. !!", "error");
        });

        rzp.open();
      }
    },
    error: function (error) {
      //invoked when error
      console.log(error);
      alert("something went wrong !!");
    },
  });
};

function updatePaymentOnServer(payment_id, order_id, status)
{
  $.ajax({
    url: "/payment/update_order",
    data: JSON.stringify({
      payment_id: payment_id,
      order_id: order_id,
      status: status,
    }),
    contentType: "application/json",
    type: "POST",
    dataType: "json",
    success: function (response) {
      swal("Good Job !!", "Congrats !! payment successfull !!", "success");
    },
    error: function (error) {
      swal(
        "Failed !!",
        "Your payment is successful. but we did not get on server,we will contact you as soon as possible",
        "error"
      );
    },
  });
}
