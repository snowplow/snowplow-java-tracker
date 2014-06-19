
## Current Build

Since production is still early and large changes are being made daily, jar files may not be up to date, check the version associated with the jar. Class files can be found in the out folder. At the moment the source files are set up as a package.

To get started, move into the src folder and follow the set up guide!

Note: The set up there is for the package "com.snowplow.javaplow", this means you should leave all source files in the folders, and drag the entire package folder into your project. 

### About the Tracker

All tracking methods have been tested successfully, but more bugs are discovered every day. If you find one, let me know by email or github.

###Ready to [view the documentation][documentation]?

Dont have the collector configured? [See Snowplow setup][setup]

## Comments

- For a full list of available functions, look into the packages interfaces.
- Context is meant to be in JSON String format, unstructured data can be string or Map<String,Object>

	String context = "{'Movie':'Shawshank Redemption', 'Time':'142 Minutes' }"
	Map<String,Object> unstruct_info = new LinkedHashMap<String,Object>();
	unstruct_info.put("Gross movie profit", 28341469);
	...

[documentation]: https://gleasonk.github.io/Saggezza/JavaDoc/index.html
[setup]: https://github.com/snowplow/snowplow/wiki/Setting-up-Snowplow
