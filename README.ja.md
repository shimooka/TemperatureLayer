Temperature Layer
=================
[Temperature Layer]はバッテリー温度を表示するだけのシンプルなAndroidアプリケーションです。
ステータスバー上のアイコンやウィジェットとして温度を表示するではなく、画面上に透明なレイヤをかぶせて表示しますので、HOME画面や他のアプリの邪魔になりません。

Temperature Layerは[Google Play]から入手できます。

APKファイルは[こちら](https://www.dropbox.com/sh/0c8u8q45v6vs8es/AAAjNQ0GiQj1sJ3LEwUiV0SHa/TemperatureLayer)からダウンロードできます。

また、ソースコード(eclipseプロジェクト形式)を[GitHub](https://github.com/shimooka/TemperatureLayer)で公開しています。ライセンスは[Apache License, Version 2.0][Apache]です。

特徴
----

- Android2.0以降に対応
- バッテリー温度を画面上の任意の位置に表示(ステータスバーの上も可能)
- 端末起動時の自動起動
- 通知領域に温度通知を表示(切り替え可能)
- ステータスバー上のアイコンの非表示 (**Android 4.2以降**対応)
- 単位は摂氏(°C)、華氏(°F)から選択
- テキストのフォント、サイズ、色(透明度も含む)をカスタマイズ可能
- 高温時のアラート機能(通知音、バイブレーション)
- メッセージは英語と日本語
- 無料です！

表示位置の変更について
----------------------

- 設定メニューの**表示位置**をタップすると、**編集モード**になります
- 編集モードでは、温度表示は**常に赤色**で表示されます。
- **スワイプ**すると表示位置を移動できます
- 編集モードの終了は、**二本指でタッチ**もしくは**ダブルタップ**します

表示位置の変更時の制限
----------------------
ステータスバーに移動させると下に隠れてしまいます。これは仕様です。編集モード終了後は正しく表示されます。

その他制限
----------------------
AndroidOSの[設定]→[アプリ]メニューで、Temperature Layerの「通知を表示」のチェックを外している場合、ステータスバーへの通知や表示位置編集時の説明トーストは表示されません。

画面キャプチャ
--------------
![All screen of Temperature Layer](capture.png)

謝辞
----
このアプリケーションは以下のライブラリを使用しています。それぞれの作者さんに感謝！

- [Android Color Picker]
- [FontPreference dialog for Android] (一部修正版)

リリース履歴
------------
- 2014/05/20 ver.1.0.4 - 表示位置の編集モードを、二本指タップもしくはダブルタップで終了するよう変更。バグ修正
- 2014/05/16 ver.1.0.3 - 表示位置を任意に変更可能。ステータスバー上のアイコン非表示(Android4.2以降)。バグ修正
- 2014/05/09 ver.1.0.2 - 高温時のアラート機能を追加
- 2013/07/26 ver.1.0.1 - フォント選択機能を追加
- 2013/06/26 ver.1.0.0 - 正式版リリース
- 2013/06/25 ver.0.9.3 - ステータスバー上にも表示可能に
- 2013/06/24 ver.0.9.2 - 内部構造を改善
- 2013/06/19 ver.0.9.1 - 最初のβ版リリース

ライセンス
----------
Copyright &copy; 2013,2014 Hideyuki SHIMOOKA &lt;shimooka@doyouphp.jp&gt; 
Licensed under the [Apache License, Version 2.0][Apache]

[Apache]: http://www.apache.org/licenses/LICENSE-2.0
[Android Color Picker]: https://code.google.com/p/android-color-picker/
[Temperature Layer]: https://play.google.com/store/apps/details?id=jp.doyouphp.android.temperaturelayer
[Google Play]: https://play.google.com/store/apps/details?id=jp.doyouphp.android.temperaturelayer
[FontPreference dialog for Android]: http://www.ulduzsoft.com/2012/01/fontpreference-dialog-for-android/
