(ns staircase.views
  (:require [hiccup.form :refer :all]
            [hiccup.def :as d]
            [persona-kit.view :as pv])
  (:use [hiccup.page :only (html5 include-css include-js)]
        [hiccup.element :only (link-to unordered-list javascript-tag mail-to)]))

(defn- with-utf8-charset [[tag attrs]]
  [tag (assoc attrs :charset "utf-8")])

(def ^:dynamic require-js "/vendor/requirejs/require.js")

(defn- entry-point [ep]
  (->> (include-js require-js)
       (map (fn [[tag attrs]] [tag (assoc attrs :data-main ep)]))
       (first)))

(def vendor-scripts [])

(def button-style :orange) ;; :orange or :dark

(defn login []
  (-> (pv/sign-in-button {:ng-click "persona.request()"} button-style)
      (update-in [2 1] (constantly "Sign in/Sign up"))))

(defn logout []
  (-> (pv/sign-in-button {:ng-click "persona.logout()"} button-style)
      (update-in [2 1] (constantly "Sign out"))))

(defn header []
  [:div.navbar.navbar-custom.navbar-default.navbar-fixed-top {:role "navigation"}
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand
      [:span.app-name "Steps"]]] ;; Make configurable?
    [:div.collapse.navbar-collapse

     [:ul.nav.navbar-nav.navbar-right {:ng-controller "AuthController"}
      [:li {:ng-show "auth.loggedIn"}
       [:a "{{auth.identity}}"]
       (logout)]
      [:li {:ng-hide "auth.loggedIn"}
       (login)];; TODO: Replace with persona button
      [:li.dropdown
       [:a.dropdown-toggle
        "get in touch!"]
       (unordered-list {:class "dropdown-menu"}
                       [(mail-to "alex.kalderimis@gmail.com" ;; TODO: Make configurable.
                                  [:span
                                  [:i.fa.fa-envelope-o]
                                  " Contact"])
                        (link-to "https://github.com/alexkalderimis/staircase"
                                 [:i.fa.fa-github]
                                 " View on github")])]]

     [:form.navbar-form.navbar-left.col-sm-5
      {:ng-controller "QuickSearchController"
       :role "search"
       :ng-submit "startQuickSearchHistory(searchTerm)"}
      [:div.input-group
       [:input.form-control {:ng-model "searchTerm" :placeholder "eve"}] ;; TODO: Definitely make placeholders injectable.
       [:span.input-group-btn
        [:button.btn.btn-default {:type "submit"}
         "Search "
         [:i.fa.fa-search]]]]]

     ]]])

(defn common
  ([title body] (common title body []))
  ([title body scripts]
   (html5
     [:head
      [:meta {:charset "utf-8"}]
      [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge,chrome=1"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1, maximum-scale=1"}]
      [:title title]
      (pv/include-persona)
      (include-css "/css/style.css")
      ]
     (concat [
              [:body {:ng-app "steps"}
               (header)
               [:section#content.main body]]
              ]
             scripts))))

(defn four-oh-four []
  (common "Page Not Found"
          [:div#four-oh-four "The page you requested could not be found"]))

(defn index []
  (common "Steps"
          [:div.container
            [:div.row
              [:div.panel.panel-default
              [:div.panel-heading
                "Welcome to " [:strong "Steps"]]
              [:div.panel-body "The data-flow interface for intermine. Take a first step:"]]]
            [:div.row.starting-points {:ng-controller "StartingPointsController"}]]
          (map with-utf8-charset
               (conj (apply include-js vendor-scripts)
                     (entry-point "/js/frontpage")))))