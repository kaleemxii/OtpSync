var inp = null;
document.addEventListener("mousedown", (e)=> inp = e.target.tagName=='INPUT' && e.button == 2? e.target:null);
chrome.runtime.onMessage.addListener((msg, sender, sendResponse) => { if(msg && inp) inp.value=msg});
console.log("loaded");