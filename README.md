# PlaylistPorter
move playlist from KKBOX to Spotify

http://www.playlistporter.tw/

---
開發筆記 2022-07-11
---
目前可將KKBOX歌單搬移至Spotify

但是實際搬移結果與輸出到畫面上的搬移結果有差異

畫面上搬移結果全都是空白(失敗)

實際到目的歌單查詢發現歌曲都有進去

查server log 發現當時Spotify api server回傳以下Error

API rate limit exceeded

https://developer.spotify.com/documentation/web-api/guides/rate-limits/

---
開發筆記 2022-07-12
---
在對Spotify大量查詢的時候增加

TimeUnit.SECONDS.sleep(spotifyRateLimits);

spotifyRateLimits 一開始設30 , 後來降到1

沒有出現 API rate limit exceeded的錯誤了

但歌單內的歌曲若太多,會發生504 timeout

---
開發筆記 2022-07-13
---
TimeUnit.SECONDS.sleep(spotifyRateLimits); 

改用

TimeUnit.MILLISECONDS.sleep(spotifyRequestDelayMilliseconds);

目前設定為每0.1秒發出一個Request,但衍生問題"多個使用者同時發送時如何控管發出Request的頻率"

本機測試不會丟504 timeout

部署到Elastic Beanstalk 測試也不會

TODO: 接畫面的Controller 試著改回傳 WebAsyncTask

---
