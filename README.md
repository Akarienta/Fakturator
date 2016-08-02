# Fakturator
My personal software for invoice creation.

Main features:
  - save your contact information
  - save contact information of your customers
  - create invocie and render it to PDF

Interface is in Czech language only and will be never translated - this software is applicable only in the Czech Republic.

### Changelog
#### 1.1
##### 2016-08-02
- new feature: date of tax on invoices
- new feature: IC is no more mandatory
- new feature: DIC is no more mandatory, choise of set a customer as VAT excluded
- improvement: graphic of invoice header
- bugfix: fixed fileds width on contractor panel
- bugfix: contractor and customer has the same width and alingment on invoices
- bugfix: prices are aligned to right
- note: if you want to generate an invoice from 1.0 version `*.xml` files just add `taxDate` node into `details` node

#### 1.0
##### 2015-04-16
- first release

### Tech
* [Java](https://www.java.com/en/) - an object-oriented programming language developed by Sun Microsystems
* [FOP](https://xmlgraphics.apache.org/fop/) - a print formatter driven by XSL formatting objects

### Installation
Check [releases](https://github.com/akarienta/fakturator/releases) and download the newest version from there, unzip it somwhere on your disc and run it. An icon is attached.

#### Run on Windows
Doubleclick on the `fakturator.jar` file.

#### Run on Linux and Mac
```sh
java -jar fakturator
```
### TODO list
 - datepicker
 - autocomplete of the day of payment
 - ... and you can suggest anything else

### License
Beerware (check the text [here](https://github.com/akarienta/fakturator/blob/master/LICENSE)).

**Free Software, Hell Yeah!**