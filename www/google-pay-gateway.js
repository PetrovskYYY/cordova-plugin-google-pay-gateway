cordova.define("cordova-plugin-google-pay-gateway.googlePayGW", function(require, exports, module) {
var googlePayGW = {
  configure: function (data) {
    return new Promise(function (resolve, reject) {
	  // console.log("run cordova-plugin-google-pay-gateway.googlePayGW configure+")
      cordova.exec(resolve, reject, 'GooglePayGateway', 'configure', [ data ])
	  // console.log("run cordova-plugin-google-pay-gateway.googlePayGW configure-")
    })
  },
  isReadyToPay: function () {
    return new Promise(function (resolve, reject) {
		// console.log("run cordova-plugin-google-pay-gateway.googlePayGW isReadyToPay+")
      cordova.exec(resolve, reject, 'GooglePayGateway', 'isReadyToPay', [])
	  // console.log("run cordova-plugin-google-pay-gateway.googlePayGW isReadyToPay-")
    })
  },
  /*requestPayment: function (totalPrice, currency) {
    return new Promise(function (resolve, reject) {
      // console.log("run cordova-plugin-google-pay-gateway.googlePayGW requestPayment+")
      cordova.exec(resolve, reject, 'GooglePayGateway', 'requestPayment', [ totalPrice, currency ])
      // console.log("run cordova-plugin-google-pay-gateway.googlePayGW requestPayment-")
    })
  },*/
  requestPayment: function (requestData) {
    return new Promise(function (resolve, reject) {
      // console.log("run cordova-plugin-google-pay-gateway.googlePayGW requestPayment2+")
      cordova.exec(resolve, reject, 'GooglePayGateway', 'requestPayment', [ requestData ])
      // console.log("run cordova-plugin-google-pay-gateway.googlePayGW requestPayment2-")
    })
  }
}

module.exports = googlePayGW
})
