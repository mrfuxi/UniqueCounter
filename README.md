## UniqueCounter

UniqueCounter is a Solr search component that counts unique values in search results in specified fields

### Parameters (assuming component name is "unique")
|Name          |Vale            |Note                |
|--------------|----------------|--------------------|
|unique        |true / **false**| Enabling component |
|unique.field  |[field_name]    | Name of field you want to examine ([details](http://wiki.apache.org/solr/SimpleFacetParameters#facet.field, "SimpleFacetParameters - facet.field")) |
|unique.method |enum/fc/fcs     | Terms enumeration method ([details](http://wiki.apache.org/solr/SimpleFacetParameters#facet.method, "SimpleFacetParameters - facet.method")) |
|unique.missing|true / **false**| Include missing values ([details](http://wiki.apache.org/solr/SimpleFacetParameters#facet.missing, "SimpleFacetParameters - facet.missing")) |

### How to set up:
- Add jar to be loaded
- Define component <searchComponent name="unique" class="com.fuxi.UniqueCounter" />
- Add component to a handler <arr name="last-components"><str>unique</str></arr>

Example of usage:

```
<?xml version="1.0" encoding="UTF-8"?>
<response>

<lst name="responseHeader">
  <int name="status">0</int>
  <int name="QTime">3</int>
  <lst name="params">
    <str name="unique">true</str>
    <arr name="unique.field">
      <str>popularity</str>
      <str>cat</str>
    </arr>
    <str name="facet">true</str>
    <str name="facet.field">popularity</str>
    <str name="facet.mincount">1</str>
    <str name="q">*</str>
    <str name="fl">id,popularity</str>
    <str name="rows">0</str>
  </lst>
</lst>
<result name="response" numFound="21" start="0">
</result>
<lst name="facet_counts">
  <lst name="facet_queries"/>
  <lst name="facet_fields">
    <lst name="popularity">
      <int name="6">5</int>
      <int name="7">4</int>
      <int name="1">2</int>
      <int name="10">2</int>
      <int name="0">1</int>
      <int name="5">1</int>
    </lst>
  </lst>
  <lst name="facet_dates"/>
  <lst name="facet_ranges"/>
</lst>
<lst name="unique">
  <int name="popularity">6</int>
  <int name="cat">15</int>
</lst>
</response>
```
