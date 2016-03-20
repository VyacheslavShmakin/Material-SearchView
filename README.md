# Material-SearchView
SearchView library based on DialogFragment

Download
--------

Gradle:

```groovy
compile 'com.github.VyacheslavShmakin.material-searchview:1.0.9'
```

Maven:

```xml
<dependency>
    <groupId>com.github.VyacheslavShmakin</groupId>
    <artifactId>material-searchview</artifactId>
    <version>1.0.9</version>
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
searchView.setOnVisibilityChangeListener(this);
searchView.setQuery("queryTest", false);
```

SearchView should be called by using your menu item:
``` java
...
@Override
public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
        case R.id.yourItemId:
            return searchView.onOptionsItemSelected(getFragmentManager(), item);
        default:
            return super.onOptionsItemSelected(item);
    }
}
...
```

