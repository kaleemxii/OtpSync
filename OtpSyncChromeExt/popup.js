// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

function displayWords() {

  chrome.storage.local.get("gcmId", function(result) {
    new QRCode("qrcode", {
      text: result.gcmId,
      width: 150,
      height: 150,
      colorDark : "#000000",
      colorLight : "#ffffff",
      correctLevel : QRCode.CorrectLevel.H
    });
  });
}

displayWords();

document.getElementById('wordSubmit').onclick = function() {
  let userWords = document.getElementById('userWords').value.trim();
  chrome.storage.local.get(['words'], function(object) {
    let newWords = object.words || [];
    newWords.push(userWords);
    chrome.storage.local.set({words: newWords});
  })
  chrome.tabs.executeScript(null, {
    file: 'content.js'
  });
}

document.getElementById('clearList').onclick = function() {
  chrome.storage.local.clear();
}
