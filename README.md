# Posts

Description: Microservice that connects with online platforms (Facebook, Twitter, Google) to upload and download posts


# GET: 

Download posts of the account specified from Twitter, Facebook and Google My Business

https://api.adboss.io/v1/posts

Parameters:

Key = API key provided by adboss service
username= user in the account

username = user - client. It is an email address

Example: https://api.adboss.io/v1/posts?key={{key}}&username={{username}}

Result:

[
  {
    "dateCreation": "Sun Apr 26 07:52:24 UTC 2020",
    "post": "Lo que viene; Lo que ya está aquí; Por @RaulPozo_voz; \nEl Covid-19 adelanta la energía del futuro: digital y verde Léelo en: @cronicaglobal https://t.co/6bdZF6Aioc",
    "answerON": false,
    "sons": [],
    "visibleWithParent": true,
    "name": "Manel Manchón",
    "fatherId": "-1",
    "id": "1254317388935442432",
    "platform": "Twitter",
    "status": "mmcias"
  },
  {
    "dateCreation": "Sun Apr 26 07:52:00 UTC 2020",
    "post": "¿Cómo especular en #PharmaMar en el lado alcista y en el bajista? por @a_iturralde 👇🏻\n\nhttps://t.co/WCHLPZH0ZI",
    "answerON": false,
    "sons": [],
    "visibleWithParent": true,
    "name": "CAPITAL RADIO",
    "fatherId": "-1",
    "id": "1254317288175484928",
    "platform": "Twitter",
    "status": "CAPITALRADIOB"
  }]

