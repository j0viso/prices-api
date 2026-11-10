Feature: get the price applicable to a product of a brand at a date (GET /prices)

  Background:
    * url baseUrl

  Scenario Outline: at <applicationDate> product 35455 of brand 1 costs <price> EUR from price list <priceList>
    Given path 'prices'
    And params { brandId: 1, productId: 35455, applicationDate: '<applicationDate>' }
    When method get
    Then status 200
    And match response == { brandId: 1, productId: 35455, priceList: <priceList>, startDate: '<startDate>', endDate: '<endDate>', price: <price>, currency: 'EUR' }

    Examples:
      | applicationDate     | priceList | startDate           | endDate             | price |
      | 2020-06-14T10:00:00 | 1         | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50 |
      | 2020-06-14T16:00:00 | 2         | 2020-06-14T15:00:00 | 2020-06-14T18:30:00 | 25.45 |
      | 2020-06-14T21:00:00 | 1         | 2020-06-14T00:00:00 | 2020-12-31T23:59:59 | 35.50 |
      | 2020-06-15T10:00:00 | 3         | 2020-06-15T00:00:00 | 2020-06-15T11:00:00 | 30.50 |
      | 2020-06-16T21:00:00 | 4         | 2020-06-15T16:00:00 | 2020-12-31T23:59:59 | 38.95 |

  Scenario Outline: nothing is found when <case>
    Given path 'prices'
    And params { brandId: <brandId>, productId: <productId>, applicationDate: '<applicationDate>' }
    When method get
    Then status 404
    And match response contains { status: 404, title: 'Price not found', detail: 'no price for product <productId> of brand <brandId> at <applicationDate>' }

    Examples:
      | case                        | brandId | productId | applicationDate     |
      | no price covers the date    | 1       | 35455     | 2020-06-13T10:00:00 |
      | the product is unknown      | 1       | 1         | 2020-06-14T10:00:00 |
      | the brand is unknown        | 2       | 35455     | 2020-06-14T10:00:00 |

  Scenario Outline: a query the service refuses: <detail>
    Given path 'prices'
    And params { brandId: <brandId>, productId: <productId>, applicationDate: '2020-06-14T10:00:00' }
    When method get
    Then status 400
    And match response contains { status: 400, title: 'Invalid query', detail: '<detail>' }

    Examples:
      | brandId | productId | detail                    |
      | 0       | 35455     | brandId must be positive  |
      | 1       | -1        | productId must be positive |
