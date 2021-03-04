# Reversi
javaを用いて開発したリバーシゲーム

#Demo
![Reversi_demo](https://user-images.githubusercontent.com/72292551/109929011-6a81c800-7d09-11eb-977c-2128c6a9b1fa.gif)

# Features
3つの戦術を持ったコンピューターとの対戦ができるReversiゲームです

# Requirement
* openjdk 14.0.2

# Usage
* コンパイルコマンド
```bash
javac -encoding UTF-8 Reversi.java
```
* 実行コマンド
```bash
java Reversi
```

# Note
* 初期設定ではプレイヤーは先攻で石の色は黒、コンピューターは白で後攻です
* 463,464行目のplayer[]の初期化内容を変更することによりプレイヤー同士、コンピューター同士の対戦や戦術の変更も可能になります。戦術は最後の整数値を1,2,3のどれかにすることにより変更可能です
* 手番の管理は石の色で行っています｡白を専攻に変更したい場合は465行目turn=whiteにすることで可能です
* 左下の数字は今の盤面上にある黒と白のコマの数です

# Author
* https://github.com/kokugosogo
