
# Wiki Bank Tag Integration
Allows the creation of bank tags from categories and monsters drops on the offical OSRS wiki

## Usage

### Bank tab from wiki category
Use the chat command (default is "btCat" but can be configured) followed by the name of the https://osrs.wiki catetegory you wish.

### Bank tab from monster drops
Use the chat command (default is "btDrops" but can be configured) followed by the name of the monster you wish to to tag the drops for.

## Examples:

### btCat
The following typed in chat

* "::btCat ores" will tag all ores and add a bank tab for them.
* "::btCat metal_bars" will tag all metal bars and add a bank tab for them.
![Plugin usage example](https://i.imgur.com/oFGGAAC.gif)

### bt drops
The following typed in chat

* "::btDrops imp" will tag all imp drops and add a bank tab for them.
![Plugin usage example](https://i.imgur.com/MvF90dz.gif)

https://i.imgur.com/MvF90dz.gifv
## Troubleshooting
* Category/monster names that are multiple words don't work.
	* This is due to subsequent words being counted as different arguments in the command
	* To fix use underscores in the category/monster name
	* :x: "::btCat quest items"
	* :heavy_check_mark: "::btCat quest_items"
	* :x: "::btDrops giant rat"
    * :heavy_check_mark: "::btDrops giant_rat"
