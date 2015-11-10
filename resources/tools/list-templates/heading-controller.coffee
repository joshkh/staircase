define ['lodash', './dialogue', 'text!./template-dialogue.html'], (L, Ctrl, View) ->

  controller = (console, scope, Modals, Q, connectTo) ->
    console.log "template scope is", scope
    scope.listName = scope.data.name
    scope.ids = scope.data.ids
    scope.type = scope.data.type
    scope.service = root: scope.data.root ? scope.data.service.root

    connect = connectTo scope.service.root
    scope.listnames = []

    getParsedTitle = ({title, name}) -> (title or name).replace /.*--> /, ''

    doit = ->

      if scope.listName?

        console.log "TOOL IS UPDATING"

        connect.then (s) => s.fetchTemplates().then (ts) =>

          for listname, values of ts
            if "im:aspect:#{scope.category.label}" in values.tags

              # scope.listnames.push listname
              if listname in scope.listnames
                null
              else
                # debugger
                scope.listnames.push getParsedTitle values

          scope.$apply()


          console.log "FINAL IS", scope.listnames
    doit()

    scope.$watch 'category', ->
      console.log "NEW VALUE IS", scope.category
      doit()

    scope.showTemplates = ->
      console.log "showing templates at #{ scope.service.root }"

      connect = connectTo scope.service.root
      injected =
        model: -> connect.then (s) -> s.fetchModel()
        templates: -> connect.then (s) -> s.fetchTemplates()
        service: -> connect

      if scope.listName?
        injected.list = -> connect.then (s) -> s.fetchList scope.listName
        injected.items = -> null
      else if scope.ids?
        injected.list = -> null
        injected.items = -> {ids: scope.ids, type: scope.type}

      modalInstance = Modals.open
        template: View
        controller: Ctrl
        size: 'lg'
        resolve: injected

      modalInstance.result.then (selectedTemplate) ->
        step =
          title: "Structured Search"
          description: "Using List #{ selectedTemplate.name } over #{ scope.listName }"
          tool: 'show-table'
          data:
            service:
              root: scope.service.root
            query: selectedTemplate
        scope.appendStep data: step

  ['$log', '$scope', '$modal', '$q', 'connectTo', controller]
