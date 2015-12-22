# Material-SearchView
SearchView library based on DialogFragment

Download
--------

Gradle:

```groovy
compile 'com.github.VyacheslavShmakin.material-searchview:1.0.3'
```

Maven:

```xml
<dependency>
    <groupId>com.github.VyacheslavShmakin</groupId>
    <artifactId>material-searchview</artifactId>
    <version>1.0.3</version>
    <type>aar</type>
</dependency>
```


Usage
-----
#### In Code
``` java
SearchView searchView = SearchView.getInstance(this);
DataAdapter adapter = new DataAdapter(this, getItems());
searchView.setSuggestionAdapter(adapter);
searchView.setOnToolbarRequestUpdateListener(this);
searchView.setOnVoiceSearchListener(this);
searchView.setQuery("queryTest", false);
```
