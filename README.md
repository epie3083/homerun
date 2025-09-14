# homerun

A Minecraft mod for a home run derby minigame.

## English

This mod allows you to play a home run minigame in Minecraft.

### How to Play

1.  **Set the Field Area (First Time Only)**
    -   Define the play area by using the command: `/homerun setpos <x1> <y1> <z1> <x2> <y2> <z2>`.
    -   These coordinates should be two opposite corners of the desired rectangular field. During the game, walls made of barrier blocks will be created at these boundaries.
    -   **Note:** You must set the position again if the server restarts or the world is reloaded.

2.  **Prepare the Game**
    -   Stand inside the field you just defined and run the command: `/homerun setting`.
    -   This will summon the barrier walls and the "ball" (a Wandering Trader).

3.  **Start the Game**
    -   Use the command `/homerun start <seconds>` to begin. The duration must be an integer between 10 and 180.
    -   Example: `/homerun start 20`

4.  **Charge Your Power**
    -   Once the game starts, you'll receive a wooden sword. Hit the "ball" repeatedly with the sword to charge up power.

5.  **Hit a Home Run!**
    -   When the time is up, you will be given a special **Home Run Bat**. Use this bat to launch the ball as far as you can!

### Commands

-   `/homerun setpos <x1> <y1> <z1> <x2> <y2> <z2>`
    -   Sets the two opposite corners of the playing field.

-   `/homerun setting`
    -   Summons the barrier walls and the "ball" (Wandering Trader). Must be run while the player is inside the field.

-   `/homerun start <seconds>`
    -   Starts the game. The time must be between 10 and 180 seconds.

-   `/homerun multiplier <value>`
    -   Sets the knockback divisor for the bat. The final launch force will be **divided** by this number. A higher value will result in a shorter flight distance.
    -   Example: `/homerun multiplier 10` sets the force to 1/10th.

-   `/homerun stop`
    -   Force-stops the current game.

-   `/homerun delball`
    -   Removes all summoned "balls".

---

## 日本語 (Japanese)

マインクラフトでホームランのミニゲームを遊べるようにするmodです。

### 遊び方

1.  **フィールドの設定 (初回のみ)**
    -   最初に、ミニゲームをプレイする場所をコマンドで設定します。
    -   `/homerun setpos <x1> <y1> <z1> <x2> <y2> <z2>`
    -   フィールドの対角線上の2点の座標を指定してください。ゲームが始まると、この範囲に**バリアブロック**の壁が生成されます。
    -   **注意**: この設定はサーバーの再起動やワールドの再読み込みを行うとリセットされるため、再度設定が必要です。

2.  **ゲームの準備**
    -   設定したフィールドの中にプレイヤーが入った状態で、以下のコマンドを実行します。
    -   `/homerun setting`
    -   壁と、球の代わりになる**行商人(Wandering Trader)**が召喚されます。

3.  **ゲームの開始**
    -   `/homerun start <秒数>` コマンドでゲームを開始します。秒数は10秒以上180秒以下の整数で設定できます。
    -   例: `/homerun start 20`

4.  **パワーをためる**
    -   ゲームが始まると**木の剣**が配布されます。制限時間内に球（行商人）を剣で殴り、パワーをためてください。

5.  **ホームラン！**
    -   制限時間になると、特別な**ホームランバット**が渡されます。このバットで球を殴り、遠くまで飛ばしましょう！

### コマンド一覧

-   `/homerun setpos <x1> <y1> <z1> <x2> <y2> <z2>`
    -   ゲームをプレイするフィールドの範囲を設定します。

-   `/homerun setting`
    -   フィールド内に壁と球（行商人）を召喚します。プレイヤーがフィールド内にいる状態で実行してください。

-   `/homerun start <秒数>`
    -   ゲームを開始します。秒数は10～180の間で設定します。

-   `/homerun multiplier <数値>`
    -   バットで飛ばす際の飛距離に関する**除数（割る数）**を設定します。最終的な飛距離がこの数値で割り算されます。
    -   つまり、**数値が大きいほど球は飛ばなくなります**。
    -   例: `/homerun multiplier 10` を設定すると、飛距離が1/10になります。

-   `/homerun stop`
    -   進行中のゲームを強制的に停止します。

-   `/homerun delball`
    -   召喚した球（行商人）をすべて削除します。