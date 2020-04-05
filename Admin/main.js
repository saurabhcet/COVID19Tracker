
/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var config = {
  "mapsApiKey": "AIzaSyB6v18mJ0YECGrVArsGslM8kirbZipzVJE",
  "firebaseApiKey": "99scjXCUEDfFXXxusRM3PoWXGnz2",
  "firebaseDatabaseURL": "https://covidtracker-aede6.firebaseio.com",
};

var app = firebase.initializeApp({
  apiKey: config.firebaseApiKey,
  databaseURL: config.firebaseDatabaseURL,
});

var database = app.database();
var markers = [];

database.ref('locations').on('value', function(data) {

  $('#loading').hide();

  var transports = data.val();
  transports = Object.keys(transports).map(function(id) {
    var transport = transports[id];
    transport.id  = id;
    transport.lat  = transport.latitude;
    transport.lng = transport.longitude;

var uluru = { lat: transport.lat, lng: transport.lng };
markers[id]  = { lat: transport.lat, lng: transport.lng };

var m = 'https://maps.googleapis.com/maps/api/staticmap?size=600x300&zoom=15&center=Brooklyn+Bridge,New+York,NY&maptype=roadmap&markers=color:blue%7Clabel:S%7C40.702147,-74.015794&key=' + config.mapsApiKey;

    transport.map = 'https://maps.googleapis.com/maps/api/staticmap?size=200x200'
        + '&markers=color:blue%7Clabel:' + transport.lat + '%7C' + transport.lat
        + ',' + transport.lng + '&key=' + config.mapsApiKey + '&zoom=15';
transport.map = m;
    return transport;
  });

  var html;
  if (!transports) {
    html = '<p class="empty">No transport locations available.</p>';
  } else {
    html = ejs.render($('#transports-template').html(), {transports: transports});
  }
  $('#transports').html(html);
});