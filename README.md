# <img src="src/main/resources/assets/kzeaddon-fabric/icon.png" width=32> KZEAddon - Fabric [![GitHub issues](https://img.shields.io/github/issues/patakapata/KZEAddon-Fabric-1.16.5?color=brightgreen)](https://github.com/patakapata/KZEAddon-Fabric-1.16.5/issues) [![GitHub license](https://img.shields.io/github/license/patakapata/KZEAddon-Fabric-1.16.5?color=brightgreen)](https://github.com/patakapata/KZEAddon-Fabric-1.16.5/blob/master/LICENSE)

***
KaedeZombieEscapeのプレイを支援するModです

[Forge版](https://github.com/tedo0627/KZEAddon)
(オリジナル)はTedoさんが作ってます

決してこのMODの不具合等を<br>
Tedoさんに報告*しない*ようにお願いします

ちなみにKZE運営には許可を取っていないので 使用する際は自己責任でどうぞ

## 前提

| Minecraft | KZEAddon-Fabric | Fabric API     | Fabric Loader |
|-----------|-----------------|----------------|---------------|
| 1.16.5    | 1.0.0           | \>=0.40.6+1.16 | \>=0.11.6     |

## 機能

* 武器の残弾数表示
* リロード時間の表示
* キルログのヒストリー
* バリアブロックの可視化
    * リソースパックからのモデル読み込み(実験的)
* サーバーリソースパックの拒否
* 銃声の音量の調整

### バリアモデルファイル構造

#### プリミティブタイプ

ファイルの先頭の行はプリミティブタイプのいずれか一つを記述する<br>
一つのモデルに付き使用できるプリミティブタイプは1種類のみ

* POINTS (点)
    * 全ての頂点は個別に点として表示される
* LINES (線)
    * 2つの頂点を1セットとして線で結ぶ
* TRIANGLES (三角形)
    * 3つの頂点を1セットとして中を塗り潰す
* QUADS (四角形)
    * 4つの頂点を1セットとして中を塗り潰す

#### コメント

`//`で始まる行は読み込まずに次の行に移動<br>
プリミティブの名前などを付けて可読性を向上させるため

#### 頂点

ブロックの中心からの距離を`,`で区切って記述し、3つの値を1セットとして扱う<br>
1行に2つ以上の頂点を記述することは不可能

#### サンプル

```
LINES
// Y軸
0.5, 0.0, 0.5
0.5, 1.0, 0.0
// X軸
0.0, 0.5, 0.5
1.0, 0.5, 0.5
// Z軸
0.5, 0.5, 0.0
0.5, 0.5, 1.0
```
