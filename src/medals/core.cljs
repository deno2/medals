(ns medals.core
  (:require
   [reagent.core :as r]
   [medals.widget :as widget]))

;; -------------------------
;; Views
(defn home-page []
  [:div [:h2 "Medals Widget Demo"]
   [:div
    [:p "Default widget with no parameters (defaults to sorted by gold medals):"]
    [widget/medals]]
   [:div
    [:p "Widget with initial sort set to 'bronze':"]
    [widget/medals "bronze"]]
   [:div
    [:p "Widget created with JavaScript, initial sort set to 'silver' via the code:"]
    [:code "<div id='medalElem'></div>" [:br]
     "<script type='text/javascript'>" [:br]
     "  medals.core.widget('medalElem', 'silver');" [:br]
     "</script>"]
    [:div#medalElem]]])

;; -------------------------
;; Initialize app
(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))


(defn init! []
  (mount-root))


(defn ^:export widget
  "Will mount an instance of the Medals widget on the element with the given elementId"
  ([elementId]
   (widget elementId "gold"))
  ([elementId sortKey]
   (r/render [widget/medals sortKey] (.getElementById js/document elementId))))
