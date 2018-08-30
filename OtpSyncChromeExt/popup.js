// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
var module = {
  options: [],
  header: [navigator.platform, navigator.userAgent, navigator.appVersion, navigator.vendor, window.opera],
  dataos: [
      { name: 'Windows Phone', value: 'Windows Phone', version: 'OS' },
      { name: 'Windows', value: 'Win', version: 'NT' },
      { name: 'iPhone', value: 'iPhone', version: 'OS' },
      { name: 'iPad', value: 'iPad', version: 'OS' },
      { name: 'Kindle', value: 'Silk', version: 'Silk' },
      { name: 'Android', value: 'Android', version: 'Android' },
      { name: 'PlayBook', value: 'PlayBook', version: 'OS' },
      { name: 'BlackBerry', value: 'BlackBerry', version: '/' },
      { name: 'Macintosh', value: 'Mac', version: 'OS X' },
      { name: 'Linux', value: 'Linux', version: 'rv' },
      { name: 'Palm', value: 'Palm', version: 'PalmOS' }
  ],
  databrowser: [
      { name: 'Chrome', value: 'Chrome', version: 'Chrome' },
      { name: 'Firefox', value: 'Firefox', version: 'Firefox' },
      { name: 'Safari', value: 'Safari', version: 'Version' },
      { name: 'Internet Explorer', value: 'MSIE', version: 'MSIE' },
      { name: 'Opera', value: 'Opera', version: 'Opera' },
      { name: 'BlackBerry', value: 'CLDC', version: 'CLDC' },
      { name: 'Mozilla', value: 'Mozilla', version: 'Mozilla' }
  ],
  init: function () {
      var agent = this.header.join(' '),
          os = this.matchItem(agent, this.dataos),
          browser = this.matchItem(agent, this.databrowser);
      
      return { os: os, browser: browser };
  },
  matchItem: function (string, data) {
      var i = 0,
          j = 0,
          html = '',
          regex,
          regexv,
          match,
          matches,
          version;
      
      for (i = 0; i < data.length; i += 1) {
          regex = new RegExp(data[i].value, 'i');
          match = regex.test(string);
          if (match) {
              regexv = new RegExp(data[i].version + '[- /:;]([\\d._]+)', 'i');
              matches = string.match(regexv);
              version = '';
              if (matches) { if (matches[1]) { matches = matches[1]; } }
              if (matches) {
                  matches = matches.split(/[._]+/);
                  for (j = 0; j < matches.length; j += 1) {
                      if (j === 0) {
                          version += matches[j] + '.';
                      } else {
                          version += matches[j];
                      }
                  }
              } else {
                  version = '0';
              }
              return {
                  name: data[i].name,
                  version: parseFloat(version)
              };
          }
      }
      return { name: 'unknown', version: 0 };
  }
}
  

function getBrowserAndOsName(){
  var e = module.init();
  return e.os.name +';'+  e.browser.name ;
};


function displayWords() {

  chrome.storage.local.get("gcmId", function(result) {
    new QRCode("qrcode", {
      text: getBrowserAndOsName()+'\t'+result.gcmId,
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
