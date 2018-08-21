// background.js

function registerCallback(registrationId) {
  if (chrome.runtime.lastError) {
    // When the registration fails, handle the error and retry the
    // registration later.
    return;
  }
  chrome.storage.local.set({'gcmId': registrationId});
  console.log("r---------------------",registrationId)
}


chrome.storage.local.get("gcmId", function(result) {
  // If already registered, bail out.
  if (result["gcmId"])
    return;

  // Up to 100 senders are allowed.
  var senderIds = ["505734415956"];
  chrome.gcm.register(senderIds, registerCallback);
});

chrome.contextMenus.create({
  'id': 'otpId', 
  'visible': false, 
  'title': 'Paste OTP', 
  "contexts": ["editable"]
  });

function unregisterCallback() {
  if (chrome.runtime.lastError) {
    // When the unregistration fails, handle the error and retry
    // the unregistration later.
    return;
  }
}

// chrome.gcm.unregister(unregisterCallback);
function pasteOtp(otp) {
  chrome.tabs.query({active: true, currentWindow: true}, function(tabs){
    if(tabs.length==0)return
    chrome.tabs.sendMessage(tabs[0].id, otp, (response) =>{
      setTimeout(()=>
        chrome.contextMenus.update('otpId',{
          title: 'Paste OTP', 
          visible:false,
          contexts:["editable"], 
          onclick: ()=> {}
        }), 60000);
    });  
  });
}

chrome.gcm.onMessage.addListener(function(message) {
  console.log(message)
  if(!message.data || !message.data.otp) return
  otp=message.data.otp
  chrome.contextMenus.update('otpId',{
    title: "Paste OTP '"+ otp +"'", 
    visible:true,
    contexts:["editable"], 
    onclick: ()=> pasteOtp(otp)
  });
  pasteOtp(otp)
});