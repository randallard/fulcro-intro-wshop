(ns holyjak.fulcro-exercises
  "This is a series of exercises to help you learn Fulcro, via the REPL. See the README.md!

  How to use:
  - Load this file into the cljs REPL (using the commands provided by your editor)
  - For each exercise:
    1. Un-comment *temporarily* the exercise you are working on (by replacing `(comment ...`
      with `(do ;comment ...`) Shadow-cljs will thus evaluate the code and it will render.
      Tip: Hard-reload the page occasionally to get rid of residues of past exercises.
    2. Read the instructions in the comments, make your changes, wait for shadow to apply them
    3. Regularly look at what your solution looks like in Fulcro Inspect (Client DB, ...) to
       get familiar with this essential tool.
    4. Call `(hint <exercises number>)` to get help. Repeated calls may provide more help.
       Even if you want to do it without help, check out all the hints eventually. They may
       contain useful insights.
    5. When done, compare your solution with mine in the `holyjak.solutions` namespace.
    6. Finally, comment-out the exercise again and go on to the next one.
  - See 'Troubleshooting and getting help during the exercises' in the README
  "
  (:require
    [holyjak.fulcro-exercises.impl :refer [hint config-and-render! show-client-db]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.fulcro.mutations :as m]))

(defn init [])

(do ;comment ; 7 load!-ing data from a remote
  (do
    ;; TASK:
    ;; Learn how to load! data and practice using Fulcro Inspect
    ;; This is similar to #5 but with merge-component! replaced with load!
    ;; We now run a mock, in-browser server (with a real Pathom).
    ;; Read on to find the task you should do.
    ;;
    ;; LEARNING OBJECTIVES:
    ;; - Use load!, with targeting
    ;; - Create Pathom resolvers
    ;; - Use the EQL and Network tabs of Fulcro Inspect
    ;; - Use load markers to track the state of data loading
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_loading_remote_data
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_targeting_adding_references_to_the_new_data_to_existing_entities
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_how_to
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_when_to_load
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/#_bonus_tracking_loading_state_with_load_markers

    ;; --- "Frontend" UI ---
    (defsc Address [_ {city :address/city}]
      {:query [:address/city]
       :ident :address/city}
      (p "City: " city))

    (defsc Player [_ {:player/keys [name address]}]
      {:query [:player/id :player/name {:player/address (comp/get-query Address)}]
       :ident :player/id}
      (li "Player: " name " lives at: " ((comp/factory Address) address)))

    (def ui-player (comp/factory Player {:keyfn :player/id}))

    (defsc Team [_ {:team/keys [name players]}]
      {:query [:team/id :team/name {:team/players (comp/get-query Player)}]
       :ident :team/id}
      (div (h2 "Team " name ":")
           (ol (map ui-player players))))

    (def ui-team (comp/factory Team {:keyfn :team/id}))

    (defsc Root7 [this {teams :teams :as props}]
      {:query [{:teams (comp/get-query Team)}]}
      (div
        ;; Code for task 2 (described further down) - un-comment and complete this code:
        ;(button {:type "button"
        ;         :onClick #(println "df/load! the data from here")} "Load data")
        (let [loading? false] ; scaffolding for TASK 5
          (cond
            loading? (p "Loading...")
            ;; ...
            :else
            (comp/fragment (h1 "Teams")
                           (map ui-team teams))))))

    ;; --- "Backend" resolvers to feed data to load! ---
    (defresolver my-very-awesome-teams [_ _] ; a global resolver
      {::pc/input  #{}
       ::pc/output [{:teams [:team/id :team/name
                             {:team/players [:player/id :player/name 
                                             ;; NOTE: We need this 👇 instead of just `:player/address` so that autocomplete
                                             ;; in Fulcro Inspect - EQL understands this is address and can get to id, city
                                             {:player/address [:address/id]}]}]}]}
      {:teams [#:team{:name "Hikers" :id :hikers
                      :players [#:player{:id 1 :name "Luna" :address {:address/id 1}}
                                #:player{:id 2 :name "Sol" :address {:address/id 2}}]}]})

    (defresolver address [_ {id :address/id}] ; an ident resolver
      {::pc/input #{:address/id}
       ::pc/output [:address/id :address/city]}
      (case id
        1 #:address{:id 1 :city "Oslo"}
        2 #:address{:id 2 :city "Trondheim"}))

    ;; Render the app, with a backend using these resolvers
    (def app7 (config-and-render! Root7 {:resolvers [address my-very-awesome-teams]}))

    ;; TODO: TASK 1 - use `df/load!` to load data from the my-very-awesome-teams
    (df/load! app7 :teams ui-team)
    ;; (Remember `(hint 7)` when in need.)
    ;; Now check Fulcro Inspect - the Transactions and Network tabs and explore the load there.
    ;; In both, click on the corresponding line to display details below. In the load's details
    ;; in the Network tab, press the [Send to query] button to show it in the EQL tab.
    ;; Run it from the EQL tab. Modify, run again.
    ;; - EQL tab - do [(Re)load Pathom Index] to get auto-completion for the queries and try to type some
    ;; - Index Explorer tab - do [Load index], explore the index (you might need to scroll up on the right side to see the selected thing)

    ;; TODO: TASK 2 - replace loading data during initialization (above) with loading them on-demand, on the button click

    ;; TODO: TASK 3 - split ident resolvers for a team and a player out of `my-very-awesome-teams`, as we did for address;
    ;;       Then play with them using Fulcro Inspect's EQL tab - fetch just the name of a particular person; ask for
    ;;       a property that does not exist (and check both the EQL tab and the Inspect's Network tab) - what does it look like?

    ;; TODO: TASK 4 - use targeting to fix a mismatch between a resolver and the UI: in `Root7`, rename `:teams` to `:all-teams`; how
    ;;       do you need to change the load! for this to work as before?
    ;;       Check in the Client DB that the changed data look as expected.

    ;; TODO: TASK 5 - Use Fulcro load markers to display "Loading..." instead of the content while loading the data (see Root7)

    ,))

(comment ; 8 Fix the graph
  (do
    ;; TASK:
    ;; Fix the code to actually show the list of cities.
    ;;
    ;; LEARNING OBJECTIVES: 
    ;; - Understand the importance of connections in the frontend data and how 
    ;;   to establish them
    ;; - Understand that you can add any arbitrary connection between components;
    ;;   they do not need to come only from the backend data
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/index.html#_components_initial_state
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/index.html#_components_query
    ;;
    ;; TIP: You need to fix 4 places, most of them in Root8
    ;;
    (defsc Menu [this {:keys [cities selected-city]}]
      {:ident (fn [] [:component/id ::Menu])
       :query [:cities :selected-city]}
      ;; Note: This is not a very good way of using a select :-) 
      (dom/select {:value (or selected-city "Select one:")
                   :onChange #(do (println "Selected city:" (.-value (.-target %)))
                                (m/set-string! this :selected-city :event %))}
        (->> (cons "Select one:" cities)
             (mapv #(dom/option {:key %, :value %} %)))))

    (def ui-menu (comp/factory Menu))

    (defsc Root8 [_ props]
      {:query []}
      (dom/div
        (h1 "Select a city!")
        (ui-menu {:TODO "fix this and other places"})))
    
    (defresolver cities [_ _]
      {::pc/input #{}
       ::pc/output [:cities]}
      {:cities ["Linköping" "Oslo" "Prague"]})

    ;; Render the app, with a backend using these resolvers
    (def app8 (config-and-render! Root8 {:resolvers [cities]}))

    ;; We want to load :cities into the Menu component because it is the only
    ;; one needing the data (rather then polluting root with it and having to
    ;; use a Link Query)
    (df/load! app8 :cities nil {:target (conj (comp/get-ident Menu {}) :cities)})
    ))

;; TODO Additional exercises:
;; - computed props for passing a callback from the parent
;; - create/delete/create+delete <> tmpids; simulated failure => undo the optimistic change?
;; - link queries & more from https://blog.jakubholy.net/2020/fulcro-divergent-ui-data/
;; - anything else from the Minim. Fulcro Tutorial should be added?
;;   - Initial state propagation?
;;   - Computed props for a callback or parent-visible prop
;;   - pre-merge ?! / loading dyn. data for a defsc containing a router x we did not learn routers
;;   - Link Query? But not covered by MFT
;; Other:
;; @peterdee:  incremental loading of big trees of data. Incremental loading is probably discussed 
;; adequately in the Developer Guide, but not with recursive queries, and incremental expansion of a UI tree, 
;; I think. If that seems like too much an edge case, maybe something simpler with trees.
