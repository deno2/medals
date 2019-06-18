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
    [:p "Widget with initial sort set to 'gold':"]
    [widget/medals "gold"]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
