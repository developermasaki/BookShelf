# BookShelf
 Google Books APIを使って、本の検索と表示、ローカルへのお気に入り保存が出来るポートフォリオアプリです。<br >
 [「Compose を用いた Android アプリ開発の基礎」](https://developer.android.com/courses/android-basics-compose/course?hl=ja)のユニット５とユニット６の学習内容を主に使用しました。

## 使用技術一覧
<p style="display: inline">
 <img src="https://img.shields.io/badge/kotlin-black?style=for-the-badge&logo=kotlin&logoColor=%237F52FF&color=black">
 <img src="https://img.shields.io/badge/android%20studio-black?style=for-the-badge&logo=androidstudio&logoColor=%233DDC84&color=black">
 <img src="https://img.shields.io/badge/figma-black?style=for-the-badge&logo=figma&logoColor=%23F24E1E&color=black">
 <img src="https://img.shields.io/badge/jetpack%20compose-black?style=for-the-badge&logo=jetpackcompose&logoColor=%234285F4&color=black">
</p>

**他のライブラリ**<br>
- Retrofit：RESTful サービス（Google Books API）と通信するため。
- Room : お気に入りの本をローカルストレージに保存するため。

## 実際に動いてる様子
### ライトモード
https://github.com/user-attachments/assets/d516a0c1-4e7d-4ae1-b309-7372ec37ec2b

### ダークモード
https://github.com/user-attachments/assets/133592c9-2d05-4ec7-b6ba-b000330b5045

## 機能一覧
- Google Books APIを使用した本の検索（Retrofitを使用）
- 本のタイトル、著者、説明、カテゴリー、出版日、出版社、価格、ページ数、ISBNといった情報の表示
- お気に入りの本を保存（Roomを使用）
- 検索履歴保持機能（Preferences Datastoreを使用）
- アニメーション（Shared Element Transitionを使用）
- 無限スクロール機能
- ダークモード対応
- キーボード自動昇降機能
- 検索ボタンとキーボードの動きが連動する機能

## こだわった点
### 1. 検索ボタンを画面の下に設置した点
通常、検索バーは画面上部に設置されますが、タップする際に指を上に持っていくのが負担になるのではないかと考えた。<br>
そこで、画面下部に設置することで片手で持ちながらも、楽に検索できるようにした。

### 2. 無限スクロール機能
更新ボタンや、一定の位置までスクロールしたら、読み込み画像が出て、少し待ってもらう間にデータをダウンロードする方法もあった。<br>
しかし、ボタンを押す煩わしさやダウンロード待ちがあると、ユーザビリティが低下すると考えた。<br>
そこで、読み込んだデータを全て表示する前に、新たなデータを読み込むことで、ユーザーの方に待ってもらうことなく、データを表示できるようにした。

### 3. 詳細画面における表示スタイル
詳細画面とは、画面上部に「Details」と表示されている画面のことである。その詳細画面のリストは2列で表示されており、右側の列はタップする前に閉じた状態になっている。
最初から閉じている目的は、著者や出版日、価格やページ数などの情報を、出来るだけスクロールせずに見れるようににするためである。
しかし、長い文章を表示する説明欄はタップして開かないと全文を見ることは出来ず、使い勝手の悪さもある。ここは改善の余地があると思う。

## 苦労した点
### Shared Transitionにおけるスコープをどう渡すか問題

**1.　BookCardが深くネストされていたため、引数でスコープを直接渡せなかった。**

以下が、引数で直接AnimatedVisibilityScopeとSharedTranstionScopeを渡す例である。
```Kotlin
@Composable
fun Example(
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) {
}
```
そこで、CompositionLocalを使用して、AnimatedVisibilityScopeとSharedTransitionScopeを渡すことにした。

---

**2. ``AnimatedVisibilityScope``を渡すには、``SharedTransitionLayout``内に``AnimatedContent``か``AnimatedVisibility``を使う必要があった。**

[公式リファレンス](https://developer.android.com/develop/ui/compose/animation/shared-elements?hl=ja#understand-scopes)にあるように、``AnimatedVisibilityScope``を渡すには、``Animated Content``か``AnimatedVisibility``を使用するしかなく、``AnimatedContent``を使用しようかと考えた。

しかし、<br>
1. ``AnimatedContent``だと、``targetState``に頼る必要があり、引数でデータを渡したかったので問題が生じる。
2. ``AnimatedVisibility``だと、単純に一つの画面が表示するか表示しないかの切り替えしかできなく、使い勝手が悪い。

そこで、今後の画面を複数追加することも考え、NavHostでルートを定義できた方が便利だと考え、NavHostを使用することを決めた。

---

**3. [公式リファレンス](https://developer.android.com/develop/ui/compose/animation/shared-elements/navigation?hl=ja)は、NavHostを使った例を紹介しているが、引数で``AnimatedVisibilityScope``と``SharedTransitionScope``を直接渡していて、1.の引数でスコープを直接渡せない問題が再燃した。**

リファレンスや質問サイトを見ても、丁度よい解決方法が見当たらなかった。そこで、一旦CompositionLocalで両方のScopeを渡そうとした。

しかし、``SharedTransitionScope``は渡せても、``AnimatedVisibilityScope``は``LazyVerticalGrid``のScopeに上書きされて、渡すことが出来なかった。

---

**4. そこで、``SharedTransitionScope``は``CompositionLocal``で渡し、``AnimatedVisibilityScope``は、``this@Composable``で渡すと解決した。**

実は、[公式リファレンス](https://developer.android.com/develop/ui/compose/animation/shared-elements?hl=ja#understand-scopes)にヒントらしきものはあった。
>CompositionLocals は、追跡するスコープが複数ある場合や、階層が深くネストされている場合に使用します。CompositionLocal を使用すると、保存して使用するスコープを厳密に選択できます。一方、コンテキスト レシーバーを使用すると、階層内の他のレイアウトが指定されたスコープを誤ってオーバーライドする可能性があります。たとえば、ネストされた AnimatedContent が複数ある場合、スコープがオーバーライドされる可能性があります。

 ## 最後に
 このBookShelfアプリにはまだ改善点や追加したい機能があるが、[「Compose を用いた Android アプリ開発の基礎」](https://developer.android.com/courses/android-basics-compose/course?hl=ja)の残り２ユニットを終わらせたいと思う。ちなみに、残りはWorkManagerと従来のViewベースのUIツールキットの使い方である。その後は、自分で開発したいと思っていた、AIを使った暗記アプリの制作をしたいと思う。
