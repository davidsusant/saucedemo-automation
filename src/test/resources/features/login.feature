@login @regression
Feature: Login Functionality
  As a user
  I want to login to SauceDemo
  So that I can access the products

  Background:
    Given I am on the login page

  @smoke @positive
  Scenario: Successful login with valid credentials
    When I enter username "standard_user" and password "secret_sauce"
    And I click on login button
    Then I should be redirected to products page
    And I should see "Products" as page title