CSC207 RPG Project
Team Information
Team Members
- Ryder Lin
- Roshan Dahal
- Nina Li
- Sadiqn
Project Description
Domain
A Java Swing turn-based role-playing game (RPG) battle application. A party of five heroes (Warrior, Paladin, Mage, Bowman, and Wizard) fights three enemies (Slime, Bat, and Mushroom). Fighters have HP, attack, defense, and speed statistics. Players select combat actions and enemy targets while the game manages turn order, damage, defending, and battle outcomes.

The application includes animated sprites and effects, fighter portraits and HP displays, a turn-order tracker, and an inventory system with healing and revival items. Assets are retrieved from Sparklin Labs' Superpowers asset packs and cached locally.
User Stories

Team Member	MVP Use Case
Roshan Dahal	Combat actions, targeting, damage, defending, skills, and battle presentation
Ryder Lin	To be confirmed
Nina Li	To be confirmed
sadiqn	To be confirmed


Entities
Fighter
Represents a hero or monster with a name, team affiliation, maximum/current HP, attack, defense, speed, and guarding state. Provides behavior for taking damage, healing, defending, and checking whether the fighter is alive.
Battle
Maintains hero and enemy teams, speed-based turn order, the active fighter, attacks, defending, turn advancement, and win/loss state.
Inventory
Stores items and quantities available to the party.
Item
Common item abstraction used by consumable item implementations.
HealingItem
Restores an eligible target's HP.
ReviveItem
Revives an eligible defeated target.
ItemTarget
Interface for entities that can be targeted by an item.
API Usage
API / Asset Source: Sparklin Labs — Superpowers Asset Packs (GitHub-hosted files)

Repository: https://github.com/sparklinlabs/superpowers-asset-packs

How it is used in the program: GameDataAccessObject downloads publicly hosted character sprites, enemy sprites, animations, backgrounds, and item artwork. The assets are cached in superpowers-cache/ for reuse. Downloads use standard HTTP requests; no authentication or API key is required. The GitHub raw-content service is an asset host, not a gameplay API.

Credits: Original artwork is attributed to Pixel-boy / Sparklin Labs. See the asset repository for its CC0 licensing information.
Design Diagrams
UML Class Diagram
To be added after reviewing the final code structure. Key classes include Battle, Fighter, Combat, Inventory, the item entities, GameDataAccessObject, BattleView, and the item use-case/controller/presenter classes.
Sequence Diagrams
To be added. Recommended sequences: Attack a Target, Defend, and Use Item.
Architecture
The project organizes code into the following packages:
src/main/java/
├── app/                 # Application entry point and wiring
├── entity/              # Battle, Fighter, Inventory, Item entities
├── use_case/            # Combat and item application logic
├── interface_adapter/   # Item controllers, presenters, view models
├── data_access/         # Asset loading, caching, item catalog
└── view/                # Swing interface and battle rendering
The application is being refined toward Clean Architecture and SOLID principles. The existing Combat.Presenter still directly references BattleViewModel, and BattleView still advances battle state; these dependencies are candidates for refactoring rather than claims of full architectural compliance.
How to Run
Requirements
- JDK 11 or newer
- Maven, or IntelliJ IDEA with Maven support
- Internet access for assets that are not already cached
Steps
1. Clone this repository and open the project in IntelliJ IDEA.
2. Allow Maven to import the pom.xml project.
3. Run src/main/java/app/Main.java (the app.Main class).
4. Select an action in the battle window. For Attack or Skill, select a living enemy target. Use Defend to guard, or Items to open the inventory.
Run the Maven build from the project root:
mvn clean test
Testing status: The uploaded repository does not currently contain a src/test/java/ test suite. Passing the Maven command alone therefore does not demonstrate unit-test coverage. Tests will be added alongside upcoming use-case PRs.
Notes
- Five heroes and three enemies are currently configured in GameDataAccessObject with predefined statistics.
- The game chooses a random battle background unless the background constant is changed.
- Skills currently use the same attack damage method as regular attacks, although they have distinct animation handling.
- The project currently supports item inventory and item use; the older README's statement that Items is only a placeholder is outdated.
- A separate Run action is not established as an implemented use case in the inspected code.
- New changes should be tracked with issues, reviewed via pull requests, and accompanied by appropriate unit tests and manual UI verification.
