
# Wiki Bank Tag Integration
Allows the creation of bank tags from categories on the offical OSRS wiki

## Usage

Use the chat command (default is "bt" can be configured) followed by the name of the https://osrs.wiki catetegory you wish

## Examples:
The following typed in chat

"::bt ores" will tag all ores and add a bank tab for them.
"::bt metal_bars" will tag all metal bars and add a bank tab for them.
![Plugin usage example](https://i.imgur.com/78rYr9T.gif)

## Troubleshooting
* Category names that are multiple words don't work.
	* This is due to subsequent words being counted as different arguments in the command
	* To fix use underscores in the category name
	* :x: "::bt quest items"
  * :heavy_check_mark: "::bt quest_items"
