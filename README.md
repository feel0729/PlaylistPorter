# PlaylistPorter
move playlist from KKBOX to Spotify

http://www.playlistporter.tw/

---
2022-07-11 開發筆記
---
目前可將KKBOX歌單搬移至Spotify
但是實際搬移結果與輸出到畫面上的搬移結果有差異

畫面上搬移結果全都是空白(失敗)
實際到目的歌單查詢發現歌曲都有進去
查server log 發現當時Spotify api server回傳以下Error
API rate limit exceeded
