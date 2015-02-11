start = File / Chunk
File
  = (Chunk "\n"?)+
Chunk
  = f:format0 {console.log(f)}
format0
  = format1 format5 " " format8 format13 format14 " " format32
format1
  = format2 " - - "
format2
  = format3
  / format4
format3
  = Int "." Int "." Int "." Int
format4
  = "::" Int
format5
  = "[" format6 "]"
format6
  = Int "/" format7 "/" Int ":" Int ":" Int ":" Int " +" Int
format7
  = "May"
  / "Mar"
  / "Jun"
  / "Jul"
  / "Apr"
format8
  = "\"" format9 "\""
format9
  = format10 " " format11 " HTTP/" Int "." Int
format10
  = "OPTIONS"
  / "POST"
  / "GET"
format11
  = format12
  / "*"
format12
  = "/c-lang/lib/bootstrap/fonts/glyphicons-halflings-regular.woff"
  / "/c-lang/lib/bootstrap/fonts/glyphicons-halflings-regular.ttf"
  / "/c-lang/lib/bootstrap/fonts/glyphicons-halflings-regular.svg"
  / "/c-lang/lib/bootstrap/fonts/glyphicons-halflings-regular.eot"
  / "/c-lang/lib/codemirror/display/placeholder.js"
  / "/c-lang/lib/bootstrap/css/bootstrap.min.css"
  / "/c-lang/lib/codemirror/mode/clike/clike.js"
  / "/c-lang/lib/bootstrap/js/bootstrap.min.js"
  / "/c-lang/lib/codemirror/codemirror.css"
  / "/c-lang/lib/codemirror/codemirror.js"
  / "/c-lang/lib/jquery.tmpl.min.js"
  / "/c-lang/cgi-bin/compile.cgi"
  / "/c-lang/lib/jquery.min.js"
  / "/c-lang/lib/FileSaver.js"
  / "/c-lang/yahoo.co.jp/"
  / "/c-lang/index.css"
  / "/c-lang/index.js"
  / "/c-lang/ua.js"
  / "/favicon.ico"
  / "/c-lang/hoge"
  / "/c-lang/"
  / "/c-lang"
format13
  = " " Int " " Int " "
format14
  = "\"" format15 "\""
format15
  = format18
  / format16
  / format22
  / format25
  / format29
  / "http://www.google.com/search"
  / "-"
format16
  = "http://www.bing.com/search?q=aspen+ynu&src=IE-" format17
format17
  = "TopResult&FORM=IE" Int "TR&conversationid="
format18
  = "http://www.bing.com/" format19 "-" format20 "-" format21
format19
  = "search?q=ASPEN+%EF%BD%" Int "nu&qs=n&form=QBRE&pq=aspen+%EF%BD%" Int "nu&sc=" Int
format20
  = Int "&sp="
format21
  = Int "&sk=&cvid=" Int "d" Int "c" Int "a" Int "c" Int "fab" Int "d" Int "cad" Int "b" Int
format22
  = "http://" format23 "/c-lang/lib/bootstrap/css/bootstrap.min.css"
format23
  = "www.ubicg.ynu.ac.jp"
  / format24
format24
  = Int "." Int "." Int "." Int
format25
  = "http://" format26 "/c-lang/" format28?
format26
  = "www.ubicg.ynu.ac.jp"
  / format27
format27
  = Int "." Int "." Int "." Int
format28
  = "#"
format29
  = "http://" format30 "/course/" format31
format30
  = Int "." Int "." Int "." Int
format31
  = "view.php?id=" Int
format32
  = "\"" format33 "\""
format33
  = "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C; .NET CLR 3.5.30729; .NET CLR 2.0.50727; .NET CLR 3.0.30729)"
  / "Mozilla/5.0 (Linux; Android 4.2.2; SO-02E Build/10.3.1.B.0.256) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.166 Mobile Safari/537.36"
  / "Mozilla/5.0 (iPod touch; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53"
  / "Mozilla/5.0 (iPod touch; CPU iPhone OS 7_1_1 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D201 Safari/9537.53"
  / "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_4 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B554a Safari/9537.53"
  / "Mozilla/5.0 (iPhone; CPU iPhone OS 7_1_2 like Mac OS X) AppleWebKit/537.51.2 (KHTML, like Gecko) Version/7.0 Mobile/11D257 Safari/9537.53"
  / "Mozilla/5.0 (iPhone; CPU iPhone OS 7_0_6 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B651 Safari/9537.53"
  / "Mozilla/5.0 (Linux; U; Android 4.0.4; ja-jp; SHL21 Build/SB300) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
  / "Mozilla/5.0 (Linux; Android 4.2.2; 301F Build/V16R44A) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.114 Mobile Safari/537.36"
  / "Mozilla/5.0 (Linux; Android 4.1.1; HTL21 Build/JRO03C) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.122 Mobile Safari/537.36"
  / "Mozilla/5.0 (iPad; CPU OS 7_0_3 like Mac OS X) AppleWebKit/537.51.1 (KHTML, like Gecko) Version/7.0 Mobile/11B511 Safari/9537.53"
  / "Mozilla/5.0 (compatible; MSIE 9.0; Windows Phone OS 7.5; Trident/5.0; IEMobile/9.0; FujitsuToshibaMobileCommun; IS12T; KDDI)"
  / "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko; Google Web Preview) Chrome/27.0.1453 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.75.14 (KHTML, like Gecko) Version/7.0.3 Safari/537.75.14"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.77.4 (KHTML, like Gecko) Version/7.0.5 Safari/537.77.4"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_3) AppleWebKit/537.76.4 (KHTML, like Gecko) Version/7.0.4 Safari/537.76.4"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.74.9 (KHTML, like Gecko) Version/7.0.2 Safari/537.74.9"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/36.0.1985.125 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.153 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/35.0.1916.114 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.137 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.131 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/34.0.1847.116 Safari/537.36"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.154 Safari/537.36"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.77.4 (KHTML, like Gecko) Version/1.0 Safari/1"
  / "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:24.0) Gecko/20140610 Firefox/24.0 PaleMoon/24.6.2"
  / "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.3; WOW64; Trident/7.0; .NET4.0E; .NET4.0C)"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534+ (KHTML, like Gecko) BingPreview/1.0b"
  / "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:27.0) Gecko/20100101 Firefox/27.0"
  / "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; WOW64; Trident/6.0; ASU2JS)"
  / "Mozilla/5.0 (Windows NT 6.1; rv:6.0) Gecko/20110814 Firefox/6.0 Google favicon"
  / "Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:28.0) Gecko/20100101 Firefox/28.0"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:27.0) Gecko/20100101 Firefox/27.0"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; rv:25.0) Gecko/20100101 Firefox/25.0"
  / "Mozilla/5.0 (Windows NT 6.2; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:30.0) Gecko/20100101 Firefox/30.0"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0"
  / "Mozilla/5.0 (compatible; bingbot/2.0; +http://www.bing.com/bingbot.htm)"
  / "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)"
  / "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0)"
  / "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko"
  / "Mozilla/5.0 (Windows NT 6.1; WOW64; Trident/7.0; rv:11.0) like Gecko"
  / "Apache/2.4.6 (Ubuntu) PHP/5.5.3-1ubuntu2 (internal dummy connection)"
  / "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.2; Trident/6.0)"
  / "Wget/1.13.4 (linux-gnu)"
export
  = Symbol_10
  / Int
  / ELSE

Symbol_10
  = [a-zA-Z]+

Int
  = [0-9]+

ELSE
  = .
