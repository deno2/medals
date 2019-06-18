(ns medals.core
  (:require
   [reagent.core :as r]))

;; -------------------------
;; Views


(defn medals
  ([]
   (medals "gold"))
  ([sort]
   [:div.medals-widget
    [:h3 "Medal Count"]]))


(defn home-page []
  [:div [:h2 "Medals Widget Demo"]
   [:div
    [:p "Default widget with no parameters (defaults to sorted by gold medals):"]
    [medals]]
   [:div
    [:p "Widget with initial sort set to 'gold':"]
    [medals "gold"]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
